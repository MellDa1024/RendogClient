package com.rendog.client.command.utils

import com.rendog.client.command.CommandBuilder
import com.rendog.client.command.args.AbstractArg
import com.rendog.client.command.args.ArgIdentifier
import com.rendog.client.command.execute.IExecuteEvent

/**
 * Type alias for a block used for execution of a argument combination
 *
 * @param E Type of [IExecuteEvent], can be itself or its subtype
 *
 * @see CommandBuilder.execute
 */
typealias ExecuteBlock<E> = suspend E.() -> Unit

/**
 * Type alias for a block used for Argument building
 *
 * @param T Type of argument
 *
 * @see CommandBuilder
 */
typealias BuilderBlock<T> = AbstractArg<T>.(ArgIdentifier<T>) -> Unit
