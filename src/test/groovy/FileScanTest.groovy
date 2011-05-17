import jsr166y.ForkJoinPool
import org.mbte.groovypp.concurrent.ForkJoinCategory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Typed class FileScanTest extends GroovyShellTestCase implements ForkJoinCategory {
    void testScan () {
        ForkJoinPool fjPool = []
        ConcurrentHashMap<Character,AtomicInteger> res = [:]
        fjPool.invokeTask(new File('.').canonicalFile) { File file ->
            if(file.directory) {
                for(f in file.listFiles().iterator()) {
                    if(f.hidden)
                       continue

                    println "forked $f"
                    fork(f)
                }
                return {}
            }
            else {
                println "done $file"
                if(file.name.endsWith('.groovy')) {
                    for(c in file.text) {
                        def cnt = res[c]
                        if(cnt == null) {
                           res.putIfAbsent(c, [])
                           cnt = res[c]
                        }
                        cnt.incrementAndGet()
                    }
                }
            }
        }

        println "----------"
        for(e in res.entrySet().iterator())
            println "${e.key} ${e.value}"
    }
}
