package net.ryandoyle.pcp;


import net.ryandoyle.pcp.pmapi.PMAPIError;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static net.ryandoyle.pcp.PMAPI.ContextType.*;

public class PMAPI implements Closeable {

    private volatile int contextId;
    /* Locks protect modification to the context id (like closing the context)
     * while there are still potentially in-progress operations */
    private final ReadWriteLock contextIdLock = new ReentrantReadWriteLock();
    private final Lock readLock = contextIdLock.readLock();
    private final Lock writeLock = contextIdLock.writeLock();


    enum ContextType {
        PM_CONTEXT_HOST(1),
        PM_CONTEXT_ARCHIVE(2),
        PM_CONTEXT_LOCAL(3);

        private final int id;

        ContextType(int id) {
            this.id = id;
        }

        private int getId() {
            return id;
        }

    }
    static {
        System.loadLibrary("pmapi-native");
    }

    public static PMAPI newLocalContext() {
        return new PMAPI(PM_CONTEXT_LOCAL, null);
    }

    public static PMAPI newHostContext(String hostname) {
        return new PMAPI(PM_CONTEXT_HOST, hostname);
    }

    public static PMAPI newArchiveContext(String archiveLocation) {
        return new PMAPI(PM_CONTEXT_ARCHIVE, archiveLocation);
    }

    private PMAPI(ContextType contextType, String hostnameOrArchive) {
        contextId = pmNewContext(contextType.getId(), hostnameOrArchive == null ? "" : hostnameOrArchive);
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public void close() throws IOException {
        atomicallyCloseWaitingForAllUsersOfTheContext();
    }

    private void atomicallyCloseWaitingForAllUsersOfTheContext() throws IOException {
        writeLock.lock();
        if (contextId > 0) {
            try {
                pmDestroyContext(contextId);
                contextId = 0;
            } catch (PMAPIError e) {
                throw new IOException(e);
            } finally {
                writeLock.unlock();
            }
        } else {
            writeLock.unlock();
        }
    }

    private native int pmNewContext(int contextType, String hostnameOrArchive);
    private native void pmDestroyContext(int contextId);

    private <T> T withContext(Supplier<T> pmOperation) {
        readLock.lock();
        if (contextId == 0) {
            try {
                throw new IllegalStateException("context is already closed");
            } finally {
                readLock.unlock();
            }
        }
        try {
            pmUseContext(contextId);
            return pmOperation.get();
        } finally {
            readLock.unlock();
        }
    }
    private native void pmUseContext(int contextId);


    public String pmGetContextHostName() {
        return withContext(() -> pmGetContextHostName0(contextId));
    }
    private native String pmGetContextHostName0(int contextId);

    /* Visible for testing exception map */
    native static void raisePmapiException(int code);


}
