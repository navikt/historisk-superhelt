package no.nav.historisk.superhelt

import org.springframework.boot.runApplication

fun main() {
    runApplication<SuperApplication>("--spring.profiles.active=dev")
}
