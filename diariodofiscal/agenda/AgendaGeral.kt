package com.example.diariodofiscal.agenda

import android.os.Bundle
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.R
import com.example.diariodofiscal.adapters.EventosAdapter
import com.example.diariodofiscal.model.Evento
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AgendaGeral : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var eventosRecyclerView: RecyclerView
    private lateinit var eventosAdapter: EventosAdapter
    private val eventosList = mutableListOf<Evento>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda_geral)

        calendarView = findViewById(R.id.calendario)
        eventosRecyclerView = findViewById(R.id.eventosRecyclerView)
        eventosAdapter = EventosAdapter()

        eventosRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AgendaGeral)
            adapter = eventosAdapter
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }.time

            loadEventos(selectedDate)
        }
    }

    private fun loadEventos(selectedDate: Date) {
        val db = FirebaseFirestore.getInstance()

        // Format selected date to match Firestore date format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateString = dateFormat.format(selectedDate)

        // Clear previous events
        eventosList.clear()

        // Query Firestore to get all condos
        db.collection("condominios")
            .get()
            .addOnSuccessListener { condos ->
                for (condo in condos) {
                    val condoId = condo.id
                    // For each condo, query its events for the selected date
                    db.collection("Condominios")
                        .document(condoId)
                        .collection("Eventos")
                        .whereEqualTo("data", selectedDateString)
                        .get()
                        .addOnSuccessListener { events ->
                            for (event in events) {
                                val evento = event.toObject(Evento::class.java)
                                eventosList.add(evento)
                            }
                            eventosAdapter.atualizarLista(eventosList)
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this@AgendaGeral,
                                "Erro ao carregar eventos do condomínio $condoId: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@AgendaGeral,
                    "Erro ao carregar condomínios: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}