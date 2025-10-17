package no.nav.historisk.superhelt

import org.springframework.boot.runApplication

fun main() {
    System.setProperty("APP_FNR_ENCRYPTOR_SECRET", "dev-secret-key-for-testing-only-not-production")
    runApplication<SuperApplication>("--spring.profiles.active=dev")
}
