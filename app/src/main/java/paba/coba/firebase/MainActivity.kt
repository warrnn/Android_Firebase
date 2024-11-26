package paba.coba.firebase

import android.os.Bundle
import android.provider.ContactsContract.Data
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    var DataProvisi = ArrayList<daftarProvinsi>()
    lateinit var lvAdapter: SimpleAdapter
    lateinit var _etProvinsi: EditText
    lateinit var _erIbukota: EditText
    var data: MutableList<Map<String, String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = Firebase.firestore

        _etProvinsi = findViewById(R.id.etProvinsi)
        _erIbukota = findViewById(R.id.etIbutkota)
        val _btnSimpan = findViewById<Button>(R.id.btnSimpan)
        val _lvData = findViewById<ListView>(R.id.lvData)

        lvAdapter = SimpleAdapter(
            this,
            data,
            android.R.layout.simple_list_item_2,
            arrayOf<String>("Pro", "Ibu"),
            intArrayOf(
                android.R.id.text1,
                android.R.id.text2
            )
        )
        _lvData.adapter = lvAdapter

        fun ReadData(db: FirebaseFirestore) {
            db.collection("tbProvinsi").get()
                .addOnSuccessListener { result ->
                    DataProvisi.clear()
                    for (document in result) {
                        val readData = daftarProvinsi(
                            document.data.get("provinsi").toString(),
                            document.data.get("ibukota").toString()
                        )
                        DataProvisi.add(readData)

                        data.clear()
                        DataProvisi.forEach {
                            val dt: MutableMap<String, String> = HashMap(2)
                            dt["Pro"] = it.provinsi
                            dt["Ibu"] = it.ibukota
                            data.add(dt)
                        }
                    }
                    lvAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Log.d("Firebase", it.message.toString())
                }
        }

        fun TambahData(db: FirebaseFirestore, Provinsi: String, Ibukota: String) {
            val dataBaru = daftarProvinsi(Provinsi, Ibukota)
            db.collection("tbProvinsi")
                .document(dataBaru.provinsi)
                .set(dataBaru)
                .addOnSuccessListener {
                    _etProvinsi.setText("")
                    _erIbukota.setText("")
                    Log.d("Firebase", "Data Berhasil Disimpan")
                    ReadData(db)
                }
                .addOnFailureListener {
                    Log.d("Firebase", it.message.toString())
                }
        }

        ReadData(db)

        _btnSimpan.setOnClickListener {
            TambahData(db, _etProvinsi.text.toString(), _erIbukota.text.toString())
            ReadData(db)
        }

        _lvData.setOnItemLongClickListener { parent, view, position, id ->
            val namaPro = data[position].get("Pro")
            if (namaPro != null) {
                db.collection("tbProvinsi")
                    .document(namaPro)
                    .delete()
                    .addOnSuccessListener {
                        Log.d("Firebase", "Data Berhasil Dihapus")
                        ReadData(db)
                    }
                    .addOnFailureListener {
                        Log.d("Firebase", it.message.toString())
                    }
            }
            true
        }
    }
}