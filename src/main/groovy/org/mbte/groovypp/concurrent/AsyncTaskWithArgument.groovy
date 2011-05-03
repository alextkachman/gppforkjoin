package org.mbte.groovypp.concurrent

@Typed abstract class AsyncFjTaskWithArg<V,A> extends AsyncFjTask<V>  implements Cloneable {
    A argument

    AsyncFjTaskWithArg<V,A> clone() {
        AsyncFjTaskWithArg<V,A> clone = super.clone()
        clone[argument: null]
    }

    protected final groovy.lang.Function0<V> split() {
        doSplit(argument)
    }

    protected final AsyncFjTaskWithArg<V,A> fork(A arg) {
        submit( arg, clone())
    }

    protected final V invoke(A arg) {
        invoke(arg, clone())
    }

    abstract groovy.lang.Function0<V> doSplit(A argument)

    String toString () {
        "${this.class.simpleName}($argument) ${parentTask ? '<- ' + parentTask : ''}"
    }
}
