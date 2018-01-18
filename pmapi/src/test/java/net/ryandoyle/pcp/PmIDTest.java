package net.ryandoyle.pcp;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PmIDTest {

    private static final int DOMAIN_DISK = 60;
    private static final int CLUSTER_DM = 54;
    private static final int ITEM_WRITE = 1;
    private static final int DISK_DM_WRITE = 251713537;

    @Test
    public void fromFields_returnsAPmIDConstructedWithPerFieldValues() {
        PmID pmID = PmID.from(DOMAIN_DISK, CLUSTER_DM, ITEM_WRITE);

        assertThat(pmID.getRawId()).isEqualTo(DISK_DM_WRITE);
    }

    @Test
    public void fromFields_throwsAnIllegalArgumentExceptionIfTheDomainIsInvalid() {

        assertThatThrownBy(() -> PmID.from(512, CLUSTER_DM, ITEM_WRITE))
                .isInstanceOf(IllegalArgumentException.class)
                .withFailMessage("adasda");
    }

    @Test
    public void fromRaw_returnsAPmIDConstructedWithTheRawValue() {

    }

}