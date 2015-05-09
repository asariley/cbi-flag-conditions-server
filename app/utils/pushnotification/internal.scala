package utils.pushnotification.internal



//boss makes connection, hands off to driver
//class ConnectionDriver(tcp actorref) extends Actor {

    //deathpact with connection

    //make assistant?
    //var assistant: ActorRef

    //deathpact with assistant

    /*
    receive
        New Push -> add to queue, if state was not SENDING, make state SENDING. ack the notification
            start -> (change state to SENDING) take from queue send on TCP, add to assistant
            ack message from TCP -> take from queue, send on TCP, add to assistant (if no items on queue change state to IDLE)
            data from TCP -> inform boss this driver is closed, inform assistant of last received identifer, assistant requeue unsent notifications with boss
        Some way to indicate backpressure... ack each added push. Sender should use ask pattern with await. On timeout retry finite number of times


        New push, send, add to unconfirmed. Pass ack through to boss
        handle unconfirmed logic. Send unsent notifications back to boss. Ids will be assigned by driver
    */


//}

/*
//assistant will track messages sent. If an error occurs it will report all messages that need to be resent. Should use ask pattern with future for this one
class Assistant(SSLStuff) extends Actor {
    //add message
    //last identifier received
    //flush all

    add message to finite sized queue. If space needed, throw out oldest
    if we learn of last ID then send all new notifications to boss. ignore acks? mailbox size?

}
*/
