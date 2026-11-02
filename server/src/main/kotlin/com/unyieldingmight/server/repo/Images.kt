package com.unyieldingmight.server.repo

import com.unyieldingmight.server.db.Tx
import java.time.Instant

data class StoredImage(val contentType: String, val data: ByteArray)

fun Tx.insertImage(contentType: String, data: ByteArray): Int = insert(
    "INSERT INTO uploaded_image (content_type, data, created_at) VALUES (?, ?, ?)", contentType, data, Instant.now(),
)

/**
 * Deletes uploads that no class or shop item uses, once they're older than [before]. The grace period keeps a photo
 * that was just uploaded while the admin is still filling in the form.
 */
fun Tx.deleteUnusedImages(before: Instant): Int = update(
    """
    DELETE FROM uploaded_image
    WHERE created_at < ?
      AND CONCAT('/images/uploads/', id) NOT IN (SELECT image_url FROM gym_class WHERE image_url IS NOT NULL)
      AND CONCAT('/images/uploads/', id) NOT IN (SELECT image_url FROM shop_item WHERE image_url IS NOT NULL)
    """,
    before,
)

fun Tx.findImage(id: Int): StoredImage? =
    queryOne("SELECT content_type, data FROM uploaded_image WHERE id = ?", id) { StoredImage(it.getString(1), it.getBytes(2)) }
