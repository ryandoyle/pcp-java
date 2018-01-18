package net.ryandoyle.pcp;

public class PmID {

    private static final int CLUSTER_OFFSET = 10;
    private static final int DOMAIN_OFFSET = 22;
    private static final int FLAG_OFFSET = 31;
    private static final int DOMAIN_MASK = 0x01_ff;
    private static final int CLUSTER_MASK = 0x0f_ff;
    private static final int ITEM_MASK = 0x03_ff;
    private final boolean flag;
    private final int domain;
    private final int cluster;
    private final int item;

    public static PmID from(int domain, int cluster, int item) {
        return new PmID(false, domain, cluster, item);
    }

    public long getRawId() {
        return ((flag ? 1 : 0) << FLAG_OFFSET) | (domain << DOMAIN_OFFSET) | (cluster << CLUSTER_OFFSET) | item;
    }

    private PmID(boolean flag, int domain, int cluster, int item) {
        this.flag = flag;
        this.domain = domain;
        this.cluster = cluster;
        this.item = item;
        validateRangesOfValues();
    }

    private void validateRangesOfValues() {
        if(domain  > DOMAIN_MASK) {
            throw new IllegalArgumentException("Invalid domain ID " + domain + ", cannot be more than " + DOMAIN_MASK);
        }
        if(cluster > CLUSTER_MASK) {
            throw new IllegalArgumentException("Invalid cluster ID " + cluster + ", cannot be more than " + CLUSTER_MASK);
        }
        if (item > ITEM_MASK) {
            throw new IllegalArgumentException("Invalid item ID " + item + ", cannot be more than " + ITEM_MASK);
        }
    }

    @Override
    public String toString() {
        return String.format("PmID{flag=%s, domain=%d, cluster=%d, item=%d}", flag, domain, cluster, item);
    }
}
