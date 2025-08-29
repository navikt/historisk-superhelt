package no.nav.historisk.superapp

import org.springframework.boot.runApplication

fun main() {
    runApplication<SuperApplication>("--spring.profiles.active=dev")
}
