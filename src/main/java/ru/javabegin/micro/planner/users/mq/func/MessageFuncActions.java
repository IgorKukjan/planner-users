package ru.javabegin.micro.planner.users.mq.func;

import lombok.Getter;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

@Service
@Getter
//работа с каналами
public class MessageFuncActions {
    private MessageFunc messageFunc;

    public MessageFuncActions(MessageFunc messageFunc) {
        this.messageFunc = messageFunc;
    }


    //отправка сообщения
    public void sendNewUserMessage(Long id){
        messageFunc.getMessageSink()
                //сообщение помещается во внутрению шину
                //внутреняя шина автоматически считывается supplier
                //и отправляется в канад spring cloud stream, который связан с rabbitmq
                .emitNext(MessageBuilder.withPayload(id).build()
                        , Sinks.EmitFailureHandler.FAIL_FAST);

        System.out.println("Message sent: " + id);
    }
}
