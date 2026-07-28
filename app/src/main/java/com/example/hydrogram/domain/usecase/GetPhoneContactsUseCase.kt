package com.example.hydrogram.domain.usecase

import android.content.Context
import android.provider.ContactsContract
import com.example.hydrogram.domain.model.PhoneContact
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPhoneContactsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend operator fun invoke(): List<PhoneContact> = withContext(Dispatchers.IO) {
        val contactsList = mutableListOf<PhoneContact>()

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )

        try {
            val cursor = context.contentResolver.query(uri, projection, null, null, null)

            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
                val phoneIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (c.moveToNext()) {
                    val name = if (nameIndex != -1) c.getString(nameIndex) ?: "Без имени" else "Без имени"
                    val rawPhone = if (phoneIndex != -1) c.getString(phoneIndex) ?: "" else ""

                    val cleanPhone = rawPhone.replace(Regex("[\\s\\-\\(\\)]"), "")

                    if (cleanPhone.isNotEmpty()) {
                        contactsList.add(PhoneContact(name = name, phone = cleanPhone))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext contactsList.distinctBy { it.phone }.sortedBy { it.name }
    }
}
