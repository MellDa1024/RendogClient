package kr.rendog.client.util

class TaskState(done: Boolean = false) {
    var done = done
        set(_) {
            field = true
        }
}