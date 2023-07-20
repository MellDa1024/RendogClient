package kr.rendog.client.event.events

import kr.rendog.client.event.*

class PushOutOfBlocksEvent : Event, ICancellable by Cancellable()
