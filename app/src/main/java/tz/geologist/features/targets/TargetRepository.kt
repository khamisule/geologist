package tz.geologist.features.targets

import android.content.Context
import tz.geologist.data.remote.GeoApiClient

/**
 * Chanzo cha targets. Offline-first: jaribu backend halisi kwanza;
 * ukishindwa (hakuna mtandao / server chini) rudi kwenye seed ya assets.
 */
interface TargetRepository {
    /** Rudisha targets + bendera kama zimetoka mtandaoni (true) au offline seed (false). */
    suspend fun load(region: TargetRegion): Result

    data class Result(val candidates: List<TargetCandidate>, val fromNetwork: Boolean)
}

class SeedTargetRepository(context: Context) : TargetRepository {
    private val seed = TargetSeedSource(context)
    override suspend fun load(region: TargetRegion) =
        TargetRepository.Result(seed.load(region), fromNetwork = false)
}

class RemoteTargetRepository(private val api: GeoApiClient = GeoApiClient()) : TargetRepository {
    override suspend fun load(region: TargetRegion) =
        TargetRepository.Result(api.getTargets(region), fromNetwork = true)
}

/**
 * Offline-first: backend -> fallback assets. Interface ile ile kama app inavyokua.
 */
class OfflineFirstTargetRepository(context: Context) : TargetRepository {
    private val remote = RemoteTargetRepository()
    private val seed = SeedTargetRepository(context)

    override suspend fun load(region: TargetRegion): TargetRepository.Result =
        try {
            remote.load(region)
        } catch (_: Exception) {
            seed.load(region)   // offline / server haipatikani -> seed halisi ya assets
        }
}
