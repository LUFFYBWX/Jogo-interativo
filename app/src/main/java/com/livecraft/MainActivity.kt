package com.livecraft
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import android.net.Uri
class MainActivity:Activity(){
 private lateinit var username:EditText;private lateinit var status:TextView;private lateinit var musicStatus:TextView
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=Color.rgb(9,12,28);window.navigationBarColor=Color.rgb(9,12,28);GameState.initialize(this);screen()}
 private fun screen(){
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(dp(24),dp(28),dp(24),dp(24));setBackgroundColor(Color.rgb(9,12,28))}
  val sc=ScrollView(this);val body=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL};sc.addView(body)
  body.addView(label("ANIME PUSH BATTLE",26f,Color.WHITE,true).apply{gravity=Gravity.CENTER},lp())
  body.addView(label("Luffy vs Naruto",18f,Color.rgb(255,190,85),true).apply{gravity=Gravity.CENTER;setPadding(0,dp(12),0,dp(30))},lp())
  body.addView(label("CONEXÃO TIKTOK",12f,0xff80b4d2.toInt(),true),lp())
  username=EditText(this).apply{setSingleLine(true);hint="@seu_usuario";setTextColor(Color.WHITE);setHintTextColor(0xff8796aa.toInt());setText(GameState.loadUsername().let{if(it.isBlank())"" else "@$it"});setPadding(dp(14),dp(12),dp(14),dp(12));setBackgroundColor(0xff202842.toInt())}
  body.addView(username,lp().apply{bottomMargin=dp(12)})
  body.addView(btn("SALVAR USUÁRIO",0xff304563.toInt()){val u=username.text.toString().trim().removePrefix("@");if(u.isBlank())username.error="Informe o usuário";else GameState.saveUsername(u)},lp().apply{bottomMargin=dp(18)})
  body.addView(label("STATUS DA LIVE",12f,0xff80b4d2.toInt(),true),lp());status=label("Desconectado",14f,0xffffcc78.toInt(),true).apply{setPadding(dp(14),dp(14),dp(14),dp(14));setBackgroundColor(0xff202842.toInt())};body.addView(status,lp().apply{bottomMargin=dp(12)})
  body.addView(btn("CONECTAR À LIVE",0xff247f65.toInt()){val u=username.text.toString().trim().removePrefix("@");if(u.isBlank()){username.error="Informe o usuário";return@btn};GameState.saveUsername(u);GameBridgeService.statusListener={m->status.text=m};try{TikTokNative.start(u);status.text="Conectando à live de @$u..."}catch(e:UnsatisfiedLinkError){status.text="Detector nativo ainda não incluído neste APK."}catch(e:Exception){status.text="Falha: ${e.message}"}},lp().apply{bottomMargin=dp(25)})
  body.addView(label("MÚSICA DE FUNDO",12f,0xff80b4d2.toInt(),true),lp())
  musicStatus=label(musicSummary(),13f,Color.WHITE,false).apply{setPadding(dp(12),dp(10),dp(12),dp(10));setBackgroundColor(0xff202842.toInt())}
  body.addView(musicStatus,lp().apply{bottomMargin=dp(8)})
  body.addView(btn("ESCOLHER MÚSICAS",0xff304563.toInt()){val i=Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="audio/*";putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)};startActivityForResult(i,410)},lp().apply{bottomMargin=dp(18)})
  body.addView(label("ARENA",12f,0xff80b4d2.toInt(),true),lp());body.addView(label("Batalha vertical. Presentes geram impactos e movem a parede. Os valores e a meta não aparecem na tela.",14f,0xffbac5d8.toInt(),false).apply{setPadding(0,0,0,dp(18))},lp())
  body.addView(btn("INICIAR BATALHA",0xffc52d43.toInt()){GameState.startGame();startActivity(Intent(this,GameActivity::class.java))},lp())
  root.addView(sc,LinearLayout.LayoutParams(-1,0,1f));setContentView(root)
 }
 private fun musicSummary()=GameState.loadMusicUris().let{if(it.isEmpty())"Nenhuma música selecionada" else "${it.size} faixa(s) selecionada(s), repetindo"}
 @Deprecated("Activity result callback")
 override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(requestCode==410&&resultCode==RESULT_OK&&data!=null){
  val uris=mutableListOf<Uri>();data.clipData?.let{clip->for(i in 0 until clip.itemCount)uris.add(clip.getItemAt(i).uri)}?:data.data?.let{uris.add(it)}
  uris.forEach{try{contentResolver.takePersistableUriPermission(it,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){}}
  GameState.saveMusicUris(uris.map{it.toString()});musicStatus.text=musicSummary()
 }}
 private fun label(t:String,s:Float,c:Int,b:Boolean)=TextView(this).apply{text=t;textSize=s;setTextColor(c);if(b)typeface=Typeface.DEFAULT_BOLD}
 private fun btn(t:String,c:Int,a:()->Unit)=Button(this).apply{text=t;isAllCaps=false;textSize=14f;typeface=Typeface.DEFAULT_BOLD;setTextColor(Color.WHITE);setBackgroundColor(c);setOnClickListener{a()}}
 private fun lp()=LinearLayout.LayoutParams(-1,-2);private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
}