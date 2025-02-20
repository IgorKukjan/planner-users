package ru.javabegin.micro.planner.users.keycloak;

import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.javabegin.micro.planner.entity.User;

import java.util.Collections;

@Service
public class KeycloakUtils {

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;


    private static Keycloak keycloak; //ссылка на единственный экземпляр объекта кс

    //создание объекта кс
    public Keycloak getInstance() {
        if (keycloak == null) { //создаем объект только 1 раз
            keycloak = KeycloakBuilder.builder()
                    .realm(realm)
                    .serverUrl(serverUrl)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .build();
        }
        return keycloak;
    }


    //создание пользователя для кс
    public Response createKeycloakUser(User user) {

        //доступ к api realm
        RealmResource realmsResource = getInstance().realm(realm); //todoapp-realm

        //доступ к api для работы с пользователями
        UsersResource usersResource = realmsResource.users();

        CredentialRepresentation credentialRepresentation = createPasswordCredentials(user.getPassword());


        UserRepresentation kcUser = new UserRepresentation();

        kcUser.setUsername(user.getEmail());
        kcUser.setCredentials(Collections.singletonList(credentialRepresentation));
        kcUser.setEmail(user.getEmail());
        kcUser.setEnabled(true);//пользователь задействован сразу
        kcUser.setEmailVerified(false);

        Response response = usersResource.create(kcUser);

        return response;
    }

    //создание пароля для пользователя
    private CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();

        passwordCredentials.setTemporary(false);//не нужно менять пароль после первого входа
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);

        return passwordCredentials;
    }

}
