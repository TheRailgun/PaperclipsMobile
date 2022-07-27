package com.example.paperclips

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import androidx.appcompat.app.AppCompatActivity
import com.example.paperclips.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.*
import java.text.NumberFormat
import kotlin.math.pow
import kotlin.random.Random

private lateinit var binding: ActivityMainBinding
class MainActivity : AppCompatActivity() {
    private var file = "saveData"
    private var currentClipsTot = 0
    private var currentPrice = 0.25
    private var currentFunds = 0.0
    private var currentInven = 0
    private var wire = 1000
    private var wireCost = 15.0
    private var wireBase = 15.0
    private var ac = 0
    private var acCost = (Math.pow(1.1,ac.toDouble())+5)
    private var marketing = 1.0
    private var marketingPrice = 100.00
    private var publicDemand = (.8/currentPrice * Math.pow(1.1,marketing-1)*10)
    private var wireEfficiency = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            FLAG_FULLSCREEN,
            FLAG_FULLSCREEN
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loadFromFile()

        binding.titleE.text = comma(currentClipsTot)
        binding.ppcE.text = money(currentPrice)
        binding.fundsE.text = money(currentFunds)
        binding.invenE.text = comma(currentInven)
        binding.pdE.text = percent(publicDemand)
        binding.wireNumE.text = comma(wire)
        binding.wireCostE.text = money(wireCost)
        binding.acNumE.text = comma(ac)
        binding.acCostE.text = money(acCost)
        binding.marketingCostE.text = money(marketingPrice)
        binding.marketinglvlE.text = comma(marketing.toInt())

        val job = GlobalScope.launch {
            start()
        }
    }

    override fun onPause() {
        super.onPause()
        var saveData = "" +
                "${currentClipsTot.toDouble()} ," +
                "$currentPrice ," +
                "$currentFunds ," +
                "${currentInven.toDouble()} ," +
                "${wire.toDouble()} ," +
                "$wireCost ," +
                "$wireBase ," +
                "${ac.toDouble()} ," +
                "$acCost ," +
                "$marketing ," +
                "$marketingPrice ," +
                "$publicDemand ," +
                "${wireEfficiency.toDouble()}"

        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = openFileOutput(file, Context.MODE_PRIVATE)
            fileOutputStream.write(saveData.toByteArray())
            Log.e("Test","Test")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

   fun readFromFile(context: Context): String? {
        var ret = ""
        try {
            val inputStream: InputStream? = context.openFileInput(file)
            if (inputStream != null) {
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var receiveString: String? = ""
                val stringBuilder = StringBuilder()
                while (bufferedReader.readLine().also { receiveString = it } != null) {
                    stringBuilder.append("\n").append(receiveString)
                }
                inputStream.close()
                ret = stringBuilder.toString()
            }
        } catch (e: FileNotFoundException) {
            Log.e("login activity", "File not found: $e")
        } catch (e: IOException) {
            Log.e("login activity", "Can not read file: $e")
        }
       Log.e("test",ret)
        return ret
    }

    fun loadFromFile(){
        val string = readFromFile(this)
        if(string!=null&&string!=""){
            Log.e("load","loading!")
            val list = string.split(" ,")
            currentClipsTot = list[0].toDouble().toInt()
            currentPrice = list[1].toDouble()
            currentFunds = list[2].toDouble()
            currentInven = list[3].toDouble().toInt()
            wire = list[4].toDouble().toInt()
            wireCost = list[5].toDouble()
            wireBase = list[6].toDouble()
            ac = list[7].toDouble().toInt()
            acCost = list[8].toDouble()
            marketing = list[9].toDouble()
            marketingPrice = list[10].toDouble()
            publicDemand = list[11].toDouble()
            wireEfficiency = list[12].toDouble().toInt()
        }
    }

    suspend fun start() {
        val playing = true
        while(playing){
            delay(300)
            //disable/enable buttons
            if(Random.nextInt(100) < 3) {
                println("Changing wireCost")
                withContext(Dispatchers.Main) {
                    wireCost = wireBase + (Random.nextInt(12)-6)
                    binding.wireCostE.text = money(wireCost)
                }
            }
            if(Random.nextInt((500).toInt()) < (publicDemand)){
                println("Sale occurring!")
                val targetSale = Math.floor(.7*Math.pow(publicDemand,1.15))
                if(currentInven>0){
                    withContext(Dispatchers.Main){
                    if(targetSale < currentInven){
                        currentFunds += targetSale * currentPrice
                        currentInven -= targetSale.toInt()
                        binding.fundsE.text = money(currentFunds)
                        binding.invenE.text = comma(currentInven)
                        println("Sold $targetSale paperclips")
                    } else {
                        currentFunds += currentInven * currentPrice
                        println("Sold $currentInven paperclips")
                        currentInven = 0
                        binding.fundsE.text = money(currentFunds)
                        binding.invenE.text = comma(currentInven)
                    }
                    }
                }
            }
            if(ac>0){
                if(ac<wire) {
                    wire -= 1 * ac
                    currentClipsTot += 1 * ac
                    currentInven += 1 * ac
                    withContext(Dispatchers.Main) {// this is used to let me update view
                        binding.wireNumE.text = comma(wire)
                        binding.titleE.text = comma(currentClipsTot)
                        binding.invenE.text = comma(currentInven)
                    }
                } else {
                    currentClipsTot += 1 * wire
                    currentInven += 1 * wire
                    wire = 0
                    withContext(Dispatchers.Main) {
                        binding.wireNumE.text = comma(wire)
                        binding.titleE.text = comma(currentClipsTot)
                        binding.invenE.text = comma(currentInven)
                    }
                }
            }
        }
    }

    private fun comma(raw:Int):String{
        return NumberFormat.getIntegerInstance().format(raw)
    }

    private fun money(raw:Double):String{
        return NumberFormat.getCurrencyInstance().format(raw)
    }

    private fun percent(raw: Double): String{
        return NumberFormat.getIntegerInstance().format(raw).plus("%")
    }

    fun market(view: View){
        if(currentFunds > marketingPrice){
            currentFunds -= marketingPrice
            marketingPrice *= 2
            marketing += 1
            publicDemand = (.8/currentPrice * 1.1.pow(marketing - 1) *10)
            binding.fundsE.text = money(currentFunds)
            binding.pdE.text = percent(publicDemand)
            binding.marketingCostE.text = money(marketingPrice)
            binding.marketinglvlE.text = comma(marketing.toInt())
        }
    }

    fun buyWire(view:View) {
        if (currentFunds > wireCost) {
            currentFunds -= wireCost
            wire += 1000 * wireEfficiency
            binding.fundsE.text = money(currentFunds)
            binding.wireNumE.text = comma(wire)
        }
    }

    fun buyClipper(view: View){
        if (currentFunds > acCost) {
            currentFunds -= acCost
            ac += 1
            acCost = (1.1.pow(ac.toDouble()) +5)
            binding.acNumE.text = comma(ac)
            binding.acCostE.text = money(acCost)
            binding.fundsE.text = money(currentFunds)
        }
    }


    fun makeClip(view: View){
        if(wire > 0) {
            currentClipsTot += 1
            currentInven += 1
            wire -= 1
            binding.wireNumE.text = comma(wire)
            binding.titleE.text = comma(currentClipsTot)
            binding.invenE.text = comma(currentInven)
        }
    }

    fun raise(view: View){
        currentPrice += .01
        binding.ppcE.text = money(currentPrice)
        publicDemand = (.8/currentPrice * 1.1.pow(marketing - 1) *10)
        binding.pdE.text = percent(publicDemand)
    }

    fun lower(view: View){
        if(currentPrice>=0.015) {
            currentPrice -= .01
            publicDemand = (.8/currentPrice * 1.1.pow(marketing - 1) *10)
            binding.ppcE.text = money(currentPrice)
            binding.pdE.text = percent(publicDemand)

        }
    }
}