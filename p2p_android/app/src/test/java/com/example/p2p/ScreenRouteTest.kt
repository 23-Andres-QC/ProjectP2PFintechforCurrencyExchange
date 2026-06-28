package com.example.p2p

import com.example.p2p.navigation.Screen
import org.junit.Assert.*
import org.junit.Test

class ScreenRouteTest {

    @Test
    fun `Screen routes are unique`() {
        val routes = listOf(
            Screen.Login.route,
            Screen.Register.route,
            Screen.Market.route,
            Screen.Profile.route,
            Screen.EditProfile.route,
            Screen.History.route,
            Screen.Publish.route
        )
        assertEquals(routes.size, routes.distinct().size)
    }

    @Test
    fun `Transaction createRoute includes id`() {
        val txnId = "abc-123"
        val route = Screen.Transaction.createRoute(txnId)
        assertTrue(route.contains(txnId))
    }

    @Test
    fun `Rating createRoute includes id`() {
        val id = "txn-456"
        val route = Screen.Rating.createRoute(id)
        assertTrue(route.contains(id))
    }
}
