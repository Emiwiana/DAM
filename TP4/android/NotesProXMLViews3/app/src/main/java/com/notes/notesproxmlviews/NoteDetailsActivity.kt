package com.notes.notesproxmlviews

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp.Companion.now
import com.google.firebase.firestore.DocumentReference
import java.io.ByteArrayOutputStream
import java.io.InputStream

class NoteDetailsActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var saveNoteBtn: ImageButton
    private lateinit var pageTitleTextView: TextView
    private lateinit var deleteNoteTextViewBtn: TextView
    private lateinit var noteImageView: ImageView
    private lateinit var pickImageBtn: Button

    private var title: String? = null
    private var content: String? = null
    private var docId: String? = null
    private var imageBase64: String? = null
    private var isEditMode: Boolean = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                val base64 = uriToBase64(imageUri)
                if (base64 != null) {
                    imageBase64 = base64
                    noteImageView.visibility = View.VISIBLE
                    noteImageView.setImageBitmap(base64ToBitmap(base64))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_details)

        titleEditText = findViewById(R.id.notes_title_text)
        contentEditText = findViewById(R.id.notes_content_text)
        saveNoteBtn = findViewById(R.id.save_note_btn)
        pageTitleTextView = findViewById(R.id.page_title)
        deleteNoteTextViewBtn = findViewById(R.id.delete_note_text_view_btn)
        noteImageView = findViewById(R.id.note_image_view)
        pickImageBtn = findViewById(R.id.pick_image_btn)

        //receive data
        title = intent.getStringExtra("title")
        content = intent.getStringExtra("content")
        docId = intent.getStringExtra("docId")
        imageBase64 = intent.getStringExtra("imageBase64")

        if (docId != null && docId!!.isNotEmpty()) {
            isEditMode = true
        }

        titleEditText.setText(title)
        contentEditText.setText(content)
        if (imageBase64 != null) {
            noteImageView.visibility = View.VISIBLE
            noteImageView.setImageBitmap(base64ToBitmap(imageBase64!!))
        }

        if (isEditMode) {
            pageTitleTextView.text = getString(R.string.edit_your_note)
            deleteNoteTextViewBtn.visibility = View.VISIBLE
        }

        saveNoteBtn.setOnClickListener { saveNote() }
        deleteNoteTextViewBtn.setOnClickListener { deleteNoteFromFirebase() }
        pickImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
    }

    private fun saveNote() {
        val noteTitle = titleEditText.text.toString()
        val noteContent = contentEditText.text.toString()
        if (noteTitle.isEmpty()) {
            titleEditText.error = "Title is required"
            return
        }

        val note = Note()
        note.setTitle(noteTitle)
        note.setContent(noteContent)
        note.setTimestamp(now())
        note.setImageBase64(imageBase64)

        saveNoteToFirebase(note)
    }

    private fun saveNoteToFirebase(note: Note) {
        val documentReference: DocumentReference = if (isEditMode) {
            Utility.getCollectionReferenceForNotes().document(docId.toString())
        } else {
            Utility.getCollectionReferenceForNotes().document()
        }

        documentReference.set(note).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Utility.showToast(this@NoteDetailsActivity, "Note added successfully")
                finish()
            } else {
                Utility.showToast(this@NoteDetailsActivity, "Failed while adding note")
            }
        }
    }

    private fun deleteNoteFromFirebase() {
        val documentReference: DocumentReference = Utility.getCollectionReferenceForNotes().document(docId.toString())
        documentReference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Utility.showToast(this@NoteDetailsActivity, "Note deleted successfully")
                finish()
            } else {
                Utility.showToast(this@NoteDetailsActivity, "Failed while deleting note")
            }
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            // Compress to reduce size as Firestore has a 1MB document limit
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun base64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
