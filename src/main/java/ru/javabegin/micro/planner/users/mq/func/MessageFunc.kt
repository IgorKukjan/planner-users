package ru.javabegin.micro.planner.users.mq.func

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.Many
import reactor.util.concurrent.Queues
import java.util.function.Supplier

//объявление каналов
@Configuration
class MessageFunc {
    //позволяет уведомлять всех подписчиков этого flux об появлении нового сообщения
     val messageSink: Many<Message<Long>> = Sinks.many()
        .multicast()
        .onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false)

    //Flux - объект контейнер, который позволяет создать очередь, куда можно помещать сообщения
    //и любой кто подписывается на этот объект будет считывать эти сообщения из очереди
    //Flux - по требованию
    @Bean
    fun newUserActionProduce(): Supplier<Flux<Message<Long>>> {
        return Supplier { messageSink.asFlux() }
    }
}
