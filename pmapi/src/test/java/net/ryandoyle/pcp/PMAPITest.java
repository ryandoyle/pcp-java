package net.ryandoyle.pcp;

import net.ryandoyle.pcp.pmapi.PMAPIError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PMAPITest {

    private PMAPI hostContext;

    @Before
    public void setUp() {
        hostContext = PMAPI.newHostContext("localhost");
    }

    @After
    public void tearDown() throws IOException {
        hostContext.close();
    }

    @Test
    public void newHostContext_shouldNotThrowAnyExceptionsIfItCanConnectToAHost() {
        PMAPI.newHostContext("localhost");
    }

    @Test
    public void newLocalContext_shouldNotThrowAnyExceptionsIfItCanConnectLocally() {
        PMAPI.newLocalContext();
    }

    @Test
    public void newHostContext_shouldThrowAnExceptionsIfItCannotConnectToTheHost() {
        assertThatThrownBy(() -> PMAPI.newHostContext("not-a-host"))
                .isInstanceOf(PMAPIError.class)
                .withFailMessage("No route to host");
    }

    @Test
    public void close_shouldNotRaiseAnyExceptionIfTheCloseCorrectlyHappens() throws IOException {
        hostContext.close();
    }

    @Test
    public void afterClosing_operationsShouldFail_withAnIllegalStateException() throws IOException {
        hostContext.close();

        assertThatThrownBy(() -> hostContext.pmGetContextHostName())
                .isInstanceOf(IllegalStateException.class)
                .withFailMessage("context is already closed");
    }

    @Test
    public void pmGetContextHostName_shouldReturnTheHostNameOfThePMCD() {
        assertThat(hostContext.pmGetContextHostName()).isNotBlank();
    }

}