package upvictoria.pm_may_ago_2025.iti_271421.p1u3.rodriguez_contreras

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    lateinit var extractedTV: TextView
    lateinit var selectBtn: Button
    lateinit var readBtn: Button
    lateinit var pauseBtn: ImageButton
    lateinit var playBtn: ImageButton

    // variable para nuestor lector q le puse speakaloid
    private var speakaloid: TextToSpeech? = null

    // variable pa almacenar el texto extraido
    var textoExtraido: String = ""

    // variables para la posicion actual del index d texto y longitud max del stack d caracteres
    var startIndex: Int = 0
    var maxLength: Int = 200

    var PICK_PDF_FILE: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // text view, boton d seleccionar archivo y d leer
        extractedTV = findViewById(R.id.idTVPDF)
        selectBtn = findViewById(R.id.idBtnSelect)
        readBtn = findViewById(R.id.idBtnLeer)
        playBtn = findViewById(R.id.idBtnPlay)
        pauseBtn = findViewById(R.id.idBtnPausa)

        readBtn.isInvisible = true
        playBtn.isGone = true
        pauseBtn.isGone = true

        // nuestro speakaloid
        speakaloid = TextToSpeech(this, this)

        // listener q envia al intent q lee el archivo seleccionado
        selectBtn.setOnClickListener {
            seleccionarArchivo()
        }

        // listener pa leer el texto extraido
        readBtn.setOnClickListener {
            readBtn.isGone = true
            pauseBtn.isGone = false
            startIndex = 0
            leerFragmentito()
        }

        // pausa pero en realidad resetea el tts
        pauseBtn.setOnClickListener {
            pauseBtn.isGone = true
            playBtn.isGone = false
            speakaloid?.stop()
        }

        playBtn.setOnClickListener {
            playBtn.isGone = true
            pauseBtn.isGone = false
            leerFragmentito()
        }

    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {

            // speakaloid mexican version
            val spanol = Locale("spa", "MEX")
            //val result = speakaloid!!.setLanguage(Locale.US)
            val result = speakaloid!!.setLanguage(spanol)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            } else {
                readBtn!!.isEnabled = true
            }
        }
    }

    private fun leerFragmentito() {
        if (startIndex >= textoExtraido.length) return
        var end = minOf(startIndex + maxLength, textoExtraido.length)

        // evitar cortar palabras
        if (end < textoExtraido.length) {
            val lastSpace = textoExtraido.lastIndexOf(' ', end)
            if (lastSpace > startIndex) {
                end = lastSpace
            }
        }

        val fragmento = textoExtraido.substring(startIndex, end)

        speakaloid?.speak(fragmento, TextToSpeech.QUEUE_FLUSH, null, "fragmento")

        // listener para avanzar startIndex
        speakaloid?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                startIndex = end
                leerFragmentito() // llama al siguiente fragmento
            }
            override fun onError(utteranceId: String?) {}
        })
    }

    public override fun onDestroy() {
        // Shutdown TTS when activity is destroyed
        if (speakaloid != null) {
            speakaloid!!.stop()
            speakaloid!!.shutdown()
        }
        super.onDestroy()
    }

    private fun seleccionarArchivo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        startActivityForResult(intent, PICK_PDF_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PDF_FILE && resultCode == RESULT_OK) {
            data?.data?.also { uri ->
                // abrimos y extraemos texto del PDF usando el URI
                extractDataFromUri(uri)
                startIndex = 0
                if (textoExtraido.isNotBlank()) {
                    readBtn.isInvisible = false
                    readBtn.isGone = false
                    pauseBtn.isGone = true
                    playBtn.isGone = true
                }
            }
        }
    }

    private fun extractDataFromUri(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val pdfReader = PdfReader(inputStream)
                val n = pdfReader.numberOfPages
                val sb = StringBuilder()
                // transformar a texto usando el stringbuilder para luego pasarlo a string
                for (i in 0 until n) {
                    sb.append(PdfTextExtractor.getTextFromPage(pdfReader, i + 1).trim())
                    sb.append("\n\n")
                }
                textoExtraido = sb.toString()
                extractedTV.text = textoExtraido
                pdfReader.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}