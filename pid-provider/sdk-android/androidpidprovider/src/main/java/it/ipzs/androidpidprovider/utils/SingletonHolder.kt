package it.ipzs.androidpidprovider.utils

internal open class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {

    private var creator: ((A) -> T)? = creator

    @Volatile
    private var instance: T? = null

    fun getInstance(args: A): T {
        val instanceFirst = this.instance
        if (instanceFirst != null) {
            return instanceFirst
        }

        return synchronized(this) {
            val instanceSecond = this.instance
            if (instanceSecond != null) {
                instanceSecond
            } else {
                val created = creator!!(args)
                this.instance = created
                this.creator = null
                created
            }
        }
    }
}