package io.github.mrrenan.myfitnesspartner.domain.exception;

/**
 * Lançada quando o serviço de IA está temporariamente indisponível
 * (cota excedida, timeout, instabilidade do provedor).
 *
 * Representa uma condição transiente — o usuário deve tentar novamente mais tarde.
 */
public class AiServiceUnavailableException extends RuntimeException {

    public AiServiceUnavailableException(String message) {
        super(message);
    }

    public AiServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
