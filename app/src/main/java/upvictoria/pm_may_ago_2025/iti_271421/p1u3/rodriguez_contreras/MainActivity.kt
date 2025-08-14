package upvictoria.pm_may_ago_2025.iti_271421.p1u3.rodriguez_contreras

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

        // click listener for our button.
        selectBtn.setOnClickListener {
            extractData()
            if (textoExtraido.isNotBlank()) {
                speakaloid?.stop()
                readBtn.isInvisible = false
                readBtn.isGone = false
                pauseBtn.isGone = true
                playBtn.isGone = true

            }
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
            val result = speakaloid!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            } else {
                readBtn!!.isEnabled = true
            }
        }
    }

    private fun extractData() {
        // try and catch block to handle extract data operation.
        try {

            // variable para nuestro pdf extracter.
            val pdfReader: PdfReader = PdfReader("res/raw/lana.pdf")

            // a variable for pages of our pdf.
            val n = pdfReader.numberOfPages

            for (i in 0 until n) {

                // appending data to extracted text from our pdf file using pdf reader.
                textoExtraido =
                    """
                 $textoExtraido${
                        PdfTextExtractor.getTextFromPage(pdfReader, i + 1).trim { it <= ' ' }
                    }
                
                 """.trimIndent()
                // to extract the PDF content from the different pages
            }

            // ponerle el extracted text a nuestro text view.
            extractedTV.setText(textoExtraido)

            // cerrar lector d pdfs
            pdfReader.close()

        }
        // exception using catch block
        catch (e: Exception) {
            e.printStackTrace()
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

}