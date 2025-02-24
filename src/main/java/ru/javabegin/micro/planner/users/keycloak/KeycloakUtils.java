package ru.javabegin.micro.planner.users.keycloak;

import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.javabegin.micro.planner.users.dto.UserDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class KeycloakUtils {

    private static final int CONFLICT = 409; // если пользователь уже существует в KC и пытаемся создать такого же

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


//    //создание пользователя для кс
//    public Response createKeycloakUser(UserDTO userDTO, List<String> roles) {
//
//        //доступ к api realm
//        RealmResource realmsResource = getInstance().realm(realm); //todoapp-realm
//
//        //доступ к api для работы с пользователями
//        UsersResource usersResource = realmsResource.users();
//
//        CredentialRepresentation credentialRepresentation = createPasswordCredentials(userDTO.getPassword());
//
//
//        UserRepresentation kcUser = new UserRepresentation();
//
//        kcUser.setUsername(userDTO.getUsername());
//        kcUser.setCredentials(Collections.singletonList(credentialRepresentation));
//        kcUser.setEmail(userDTO.getEmail());
//        kcUser.setEnabled(true);//пользователь задействован сразу
//        kcUser.setEmailVerified(false);

    /// /        kcUser.setRealmRoles(roles);
//
//        Response response = usersResource.create(kcUser);
//
//        return response;
//    }
    public Response createKeycloakUser(UserDTO userDTO, List<String> roles) {

        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
//        user.setFirstName(userDTO.getFirstName());
//        user.setLastName(userDTO.getLastName());
        user.setEmailVerified(false);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setValue(userDTO.getPassword());
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

        List<CredentialRepresentation> list = new ArrayList<>();
        list.add(credentialRepresentation);
        user.setCredentials(list);

        UsersResource usersResource = getUsersResource();

        Response response = usersResource.create(user);

        return response;
    }

    private UsersResource getUsersResource() {
        RealmResource realm1 =  getInstance().realm(realm);
        return realm1.users();
    }


    //создание пароля для пользователя
    private CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();

        passwordCredentials.setTemporary(false);//не нужно менять пароль после первого входа
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);

        return passwordCredentials;
    }

    // поиск уникального пользователя
    public UserRepresentation findUserById(String userId) {
        RealmResource realmsResource = getInstance().realm(realm);
        UsersResource usersResource = realmsResource.users();

        // получаем пользователя
        return usersResource.get(userId).toRepresentation();
    }

    // поиск пользователя по любым атрибутам (вхождение текста)
    public List<UserRepresentation> searchKeycloakUsers(String text) {
        RealmResource realmsResource = getInstance().realm(realm);
        UsersResource usersResource = realmsResource.users();

        // получаем пользователя
        return usersResource.searchByAttributes(text);
    }


    // обновление пользователя для KC
//    public void updateKeycloakUser(UserDTO userDTO) {
//        RealmResource realmsResource = getInstance().realm(realm);
//        UsersResource usersResource = realmsResource.users();
//
//        // данные пароля - специальный объект-контейнер CredentialRepresentation
//        CredentialRepresentation credentialRepresentation = createPasswordCredentials(userDTO.getPassword());
//
//        // какие поля обновляем
//        UserRepresentation kcUser = new UserRepresentation();
//        kcUser.setUsername(userDTO.getUsername());
//        kcUser.setCredentials(Collections.singletonList(credentialRepresentation));
//        kcUser.setEmail(userDTO.getEmail());
//
//        // получаем пользователя
//        UserResource uniqueUserResource = usersResource.get(userDTO.getId());
//        uniqueUserResource.update(kcUser); // обновление
//    }

//    public void updateKeycloakUser(UserDTO userDTO) {
//
//        UserRepresentation user = new UserRepresentation();
////        user.setEnabled(true);
//        user.setUsername(userDTO.getUsername());
//        user.setEmail(userDTO.getEmail());
////        user.setFirstName(userDTO.getFirstName());
////        user.setLastName(userDTO.getLastName());
////        user.setEmailVerified(false);
//
//        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
//        credentialRepresentation.setValue(userDTO.getPassword());
//        credentialRepresentation.setTemporary(false);
//        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
//
//        List<CredentialRepresentation> list = new ArrayList<>();
//        list.add(credentialRepresentation);
//        user.setCredentials(list);
//
//        UsersResource usersResource = getUsersResource();
//
//        // получаем пользователя
//        UserResource uniqueUserResource = usersResource.get(userDTO.getId());
//        uniqueUserResource.update(user); // обновление
//    }


    // удаление пользователя для KC
    public void deleteKeycloakUser(String userId) {
        RealmResource realmsResource = getInstance().realm(realm);
        UsersResource usersResource = realmsResource.users();

        // получаем пользователя
        UserResource uniqueUserResource = usersResource.get(userId);
        uniqueUserResource.remove();

    }

    public void updateKeycloakUser(UserDTO userDTO) {


        UserResource userResource = getUserResource(userDTO.getId());
        CredentialRepresentation credentialRepresentation=new CredentialRepresentation();
        credentialRepresentation.setValue(userDTO.getPassword());
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setTemporary(false);


        UserRepresentation kcUser = new UserRepresentation();
//        kcUser.setUsername(userDTO.getUsername());
        kcUser.setCredentials(Collections.singletonList(credentialRepresentation));
        kcUser.setEmail(userDTO.getEmail());

        userResource.update(kcUser);
    }


    public UserResource getUserResource(String userId){
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }
}
