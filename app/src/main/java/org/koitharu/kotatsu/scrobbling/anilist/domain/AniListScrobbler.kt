package org.koitharu.kotatsu.scrobbling.anilist.domain

import org.koitharu.kotatsu.core.db.MangaDatabase
import org.koitharu.kotatsu.scrobbling.anilist.data.AniListRepository
import org.koitharu.kotatsu.scrobbling.domain.Scrobbler
import org.koitharu.kotatsu.scrobbling.domain.model.ScrobblerService
import org.koitharu.kotatsu.scrobbling.domain.model.ScrobblingStatus
import javax.inject.Inject
import javax.inject.Singleton

private const val RATING_MAX = 10f

@Singleton
class AniListScrobbler @Inject constructor(
	private val repository: AniListRepository,
	db: MangaDatabase,
) : Scrobbler(db, ScrobblerService.ANILIST, repository) {

	init {
		statuses[ScrobblingStatus.PLANNED] = "planned"
		statuses[ScrobblingStatus.READING] = "watching"
		statuses[ScrobblingStatus.RE_READING] = "rewatching"
		statuses[ScrobblingStatus.COMPLETED] = "completed"
		statuses[ScrobblingStatus.ON_HOLD] = "on_hold"
		statuses[ScrobblingStatus.DROPPED] = "dropped"
	}

	override suspend fun updateScrobblingInfo(
		mangaId: Long,
		rating: Float,
		status: ScrobblingStatus?,
		comment: String?,
	) {
		val entity = db.scrobblingDao.find(scrobblerService.id, mangaId)
		requireNotNull(entity) { "Scrobbling info for manga $mangaId not found" }
		repository.updateRate(
			rateId = entity.id,
			mangaId = entity.mangaId,
			rating = rating * RATING_MAX,
			status = statuses[status],
			comment = comment,
		)
	}
}
