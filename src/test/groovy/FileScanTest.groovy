import jsr166y.ForkJoinPool
import org.mbte.groovypp.concurrent.ForkJoinCategory
import org.mbte.groovypp.concurrent.AsyncFjTask

@Typed class FileScanTest extends GroovyShellTestCase implements ForkJoinCategory {
    void testScan () {
        ForkJoinPool fjPool = []
        def res = [:]
        fjPool.invokeTask(new File('.').canonicalFile) { File file ->
            if(file.directory) {
                List<AsyncFjTask> subtasks = []
                for(f in file.listFiles().iterator()) {
                    if(f.hidden)
                       continue

                    println "forked $f"
                    subtasks << fork(f)
                }

                return { complete(null) }
            }
            else {
                println "done $file"
                if(file.name.endsWith('.groovy')) {
                    for(c in file.text) {
                        synchronized(res) {
                            def cnt = res[c]
                            if(!cnt)
                                res[c] = 1
                            else
                                res[c] = ((Integer)cnt) + 1
                        }
                    }
                }
                complete(null)
            }
        }

        println "----------"
        for(e in res.entrySet().iterator())
            println "${e.key} ${e.value}"
    }
}
