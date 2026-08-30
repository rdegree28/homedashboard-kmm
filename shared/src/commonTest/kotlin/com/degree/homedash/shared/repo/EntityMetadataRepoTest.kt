package com.degree.homedash.shared.repo

import com.degree.homedash.shared.dao.AuthDao
import com.degree.homedash.shared.dao.AuthRepo
import com.degree.homedash.shared.dao.FeatureFlagDao
import com.degree.homedash.shared.model.AuthUser
import com.degree.homedash.shared.model.FeatureFlag
import com.degree.homedash.shared.model.device_metadata.DeviceMetadata
import com.degree.homedash.shared.model.device_metadata.NavigationMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Guards the rosters the screens render. A card that shouldn't be there is a dashboard someone can
 * open, and a duplicated or misspelled entity id is a control that silently never works.
 */
class EntityMetadataRepoTest {

    @Test
    fun theOfficeCardIsHiddenFromUsersWithoutItsFlag() {
        val repo = repoFor(user = "Guest", flags = emptySet())

        val destinations = repo.loadHomeScreenMetadataList().destinations()

        assertFalse(NavigationMetadata.NavigationTarget.Office in destinations)
        // Everything ungated still shows.
        assertTrue(NavigationMetadata.NavigationTarget.Pets in destinations)
    }

    @Test
    fun theOfficeCardShowsForUsersHoldingItsFlag() {
        val repo = repoFor(user = "Rob", flags = setOf(FeatureFlag.ViewOfficeScreen))

        assertTrue(NavigationMetadata.NavigationTarget.Office in repo.loadHomeScreenMetadataList().destinations())
    }

    @Test
    fun aLoggedOutVisitorGetsNoGatedCards() {
        // No user means no flags, rather than every flag.
        val repo = repoFor(user = null, flags = FeatureFlag.entries.toSet())

        assertFalse(NavigationMetadata.NavigationTarget.Office in repo.loadHomeScreenMetadataList().destinations())
    }

    @Test
    fun theFlagIsReadPerCallSoSigningInRevealsTheCard() {
        val dao = FakeAuthDao()
        val repo = EntityMetadataRepo(
            featureFlagDao = FakeFeatureFlagDao(setOf(FeatureFlag.ViewOfficeScreen)),
            authRepo = AuthRepo(dao),
        )

        assertFalse(NavigationMetadata.NavigationTarget.Office in repo.loadHomeScreenMetadataList().destinations())

        dao.user.value = AuthUser("Rob")

        assertTrue(NavigationMetadata.NavigationTarget.Office in repo.loadHomeScreenMetadataList().destinations())
    }

    @Test
    fun everyRosterEntryHasItsOwnEntityId() {
        val repo = repoFor(user = "Rob", flags = FeatureFlag.entries.toSet())
        val rosters = listOf(
            repo.loadOfficeEntityMetadataList(),
            repo.loadLivingRoomEntityMetadataList(),
            repo.loadBedroomEntityMetadataList(),
            repo.loadPetsEntityMetadataList(),
            repo.loadPlantsEntityMetadataList(),
        )

        // A screen keys its list by entity id, so a duplicate would drop a control off the grid.
        rosters.forEach { roster ->
            val ids = roster.map { it.entityId }
            assertEquals(ids.size, ids.toSet().size, "duplicate entity id in $ids")
        }
    }

    @Test
    fun everyRosterEntryNamesARealHomeAssistantDomain() {
        val repo = repoFor(user = "Rob", flags = FeatureFlag.entries.toSet())
        val rosters = repo.loadOfficeEntityMetadataList() +
            repo.loadLivingRoomEntityMetadataList() +
            repo.loadBedroomEntityMetadataList() +
            repo.loadPetsEntityMetadataList() +
            repo.loadPlantsEntityMetadataList() +
            repo.loadHomeThermostatMetadataList()

        // "trigger." is ours — a scene card's id is synthetic, since HA has no such domain.
        val domains = setOf("light", "fan", "switch", "sensor", "binary_sensor", "climate", "trigger")
        rosters.forEach { metadata ->
            val domain = metadata.entityId.substringBefore('.')
            assertTrue(domain in domains, "unexpected domain '$domain' in ${metadata.entityId}")
            assertTrue(metadata.entityId.contains('.'), "${metadata.entityId} has no domain")
            assertTrue(metadata.displayName.isNotBlank(), "${metadata.entityId} has no label")
        }
    }
}

private fun List<DeviceMetadata>.destinations() =
    filterIsInstance<NavigationMetadata>().map { it.destination }

private fun repoFor(user: String?, flags: Set<FeatureFlag>) = EntityMetadataRepo(
    featureFlagDao = FakeFeatureFlagDao(flags),
    authRepo = AuthRepo(FakeAuthDao(user?.let(::AuthUser))),
)

/** Hands out [flags] for whoever asks. */
private class FakeFeatureFlagDao(private val flags: Set<FeatureFlag>) : FeatureFlagDao {
    override fun getFeatureFlagsForUser(user: AuthUser): Set<FeatureFlag> = flags
}

/** In-memory session; [user] can be changed mid-test to stand in for signing in. */
private class FakeAuthDao(initial: AuthUser? = null) : AuthDao {
    val user = MutableStateFlow(initial)
    override fun load(): StateFlow<AuthUser?> = user
    override suspend fun save(user: AuthUser) { this.user.value = user }
    override suspend fun clear() { user.value = null }
}
