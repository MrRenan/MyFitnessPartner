package io.github.mrrenan.myfitnesspartner;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste de carga do contexto Spring completo.
 * Marcado como "integration" porque sobe o contexto com o perfil dev,
 * exigindo PostgreSQL, Redis e a chave da IA — infra que só existe
 * localmente (via Docker Compose), não no CI de testes unitários.
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("dev")
class MyFitnessPartnerApplicationTests {

	@Test
	void contextLoads() {
	}

}
