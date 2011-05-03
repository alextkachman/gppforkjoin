package org.mbte.groovypp.concurrent

import jsr166y.ForkJoinPool

@Trait abstract class ForkJoinCategory {
    static <V,E> List<V> map(ForkJoinPool pool, Iterator<E> iter, AsyncFjTaskWithArg<V,E> task) {
        pool.invokeTask { ->
            List<AsyncFjTask> tasks = []
            for(e in iter)
                tasks << submit(e, task.clone())

            return {
                List<V> res = []
                for(t in tasks)
                    res << t.join()
                res
            }
        }
    }

    static <V,E> List<V> map(ForkJoinPool pool, Iterable<E> iter, AsyncFjTaskWithArg<V,E> task) {
        pool.map(iter.iterator(), task)
    }

    static <V,A> V invokeTask(ForkJoinPool pool, A argument, AsyncFjTaskWithArg<V, A> task) {
        pool.invoke(task[argument:argument])
    }


    static <V> V invokeTask(ForkJoinPool pool, AsyncFjTask<V> task) {
        pool.invoke task
    }

    static <V> AsyncFjTask<V> submitTask(ForkJoinPool pool, AsyncFjTask<V> task) {
        pool.submit task
    }
}
