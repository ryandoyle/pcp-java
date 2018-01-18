package net.ryandoyle.pcp.pmapi;

public abstract class PMAPIError extends RuntimeException {

    PMAPIError(String message) {
        super(message);
    }

}
