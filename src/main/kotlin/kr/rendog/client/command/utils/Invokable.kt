package kr.rendog.client.command.utils

import kr.rendog.client.command.execute.IExecuteEvent

/**
 * Interface for class that can be invoked with an [IExecuteEvent]
 *
 * @param E Type of [IExecuteEvent], can be itself or its subtype
 */
interface Invokable<E : IExecuteEvent> {

    /**
     * Invoke this with [event]
     */
    suspend fun invoke(event: E)

}
