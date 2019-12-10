package com.example.myapplication

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.Button
import android.content.Intent
import android.util.Base64
import java.lang.Error
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "KotlinActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val buttonClick= findViewById<Button>(R.id.button1)
        buttonClick?.setOnClickListener {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
        }

        basicReadWrite()
    }



    fun basicReadWrite() {
        // [START write_message]
        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users/vikramk9852/data")
	val secretKey = 1835

        // [END write_message]

        // [START read_message]
        // Read from the database
        var     myClipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                try {
                    //testing encryption
//                    val encryptedString = encrypt("This is the string", "password")
//                    Log.w(TAG, "encrypted string is $encryptedString")
//                    val temp = decrypt(encryptedString.toString(), "password")
//                    Log.w(TAG, "decrypted string is $temp")
                    val value = dataSnapshot.getValue(String::class.java)
//                    Log.w(TAG, "value is $value")
//                    val decryptedString = decrypt(value.toString(), "345678900--===-0")
                    var alteredValue = ""
                    for (ch in value.orEmpty()){
                        alteredValue += (ch.toInt() xor secretKey).toChar()
                    }
                    val myClip: ClipData = ClipData.newPlainText("note_copy", alteredValue)
                    myClipboard.setPrimaryClip(myClip)
                }catch (error: Error){
                    Log.w(TAG, "[Error] Reason $error")
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
        // [END read_message]
        myClipboard.addPrimaryClipChangedListener {
            val primaryClipData= myClipboard.primaryClip
	    val secretKey = 1835

            if (primaryClipData == null || primaryClipData.itemCount > 0 && primaryClipData.getItemAt(0).text == null)
                return@addPrimaryClipChangedListener  // ... whatever just don't go to next line
            try {
                val clip = primaryClipData.getItemAt(0).text.toString()
//                val encryptedString = encrypt(clip, "345678900--===-0")
                var alteredValue = ""
                for (ch in clip){
                    alteredValue += (ch.toInt() xor secretKey).toChar()
                }
                myRef.setValue(alteredValue)
                Log.w(TAG, "clipboard changed to $alteredValue")
            }
            catch (error: Error){
                Log.w(TAG, "[Error] Reason $error")
            }

        }
    }

    @Throws(Exception::class)
    fun encrypt(text: String, password: String?): String?
    {
        if (password == null)
            return null

        val charset = Charsets.UTF_8
        val hash = hashString("SHA-256", password)
        Log.w(TAG, "hash is $hash")
        val keySpec = SecretKeySpec(hash.toByteArray(charset), "AES")
        val ivSpec = IvParameterSpec("abcd12348932f321".toByteArray(charset))
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        val results = cipher.doFinal(text.toByteArray())

        return Base64.encodeToString(results, Base64.NO_WRAP or Base64.DEFAULT)

    }


    @Throws(Exception::class)
    fun decrypt(text: String, password: String?): String?
    {
        if (password == null)
            return null
        val charset = Charsets.UTF_8
        val hash = hashString("SHA-256", password)
        val keySpec = SecretKeySpec(hash.toByteArray(charset), "AES")
        val ivSpec = IvParameterSpec("abcd12348932f321".toByteArray(charset))
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        return String(cipher.doFinal(Base64.decode(text, Base64.DEFAULT)))
    }

    private fun hashString(type: String, input: String): String {
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
//            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }
}

