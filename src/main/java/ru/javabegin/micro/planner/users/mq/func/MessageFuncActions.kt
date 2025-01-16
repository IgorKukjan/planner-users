package ru.javabegin.micro.planner.users.mq.func

import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import reactor.core.publisher.Sinks

@Service
//работа с каналами
class MessageFuncActions(private val messageFunc: MessageFunc) {
    //отправка сообщения
    fun sendNewUserMessage(id: Long) {
        messageFunc.messageSink //сообщение помещается во внутрению шину
            //внутреняя шина автоматически считывается supplier
            //и отправляется в канад spring cloud stream, который связан с rabbitmq
            .emitNext(
                MessageBuilder.withPayload<Long>(id).build(),
                Sinks.EmitFailureHandler.FAIL_FAST
            )

        println("Message sent: $id")
    }
}
