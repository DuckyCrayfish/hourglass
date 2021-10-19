package net.lavabucket.hourglass.message;

/** Target destination of a template message. */
public enum MessageTargetType {
    /** Targets all players on the server. */
    ALL,
    /** Targets all players in the associated dimension. */
    DIMENSION,
    /** Targets all sleeping players in the dimension. */
    SLEEPING,
    /** Targets all awake players in the dimension. */
    AWAKE
}
