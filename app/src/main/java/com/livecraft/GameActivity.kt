package com.livecraft

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.*
import android.os.Bundle
import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import android.view.WindowManager
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class GameActivity:Activity(){
 private var musicPlayer:MediaPlayer?=null;private var musicIndex=0;private var musicTracks:List<String> = emptyList()
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState);requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
  window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
  @Suppress("DEPRECATION")
  window.decorView.systemUiVisibility=(View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
  setContentView(BattleView(this));musicTracks=GameState.loadMusicUris();playNextTrack()
 }
 override fun onBackPressed(){GameState.stopGame();super.onBackPressed()}
 override fun onDestroy(){musicPlayer?.setOnCompletionListener(null);musicPlayer?.release();musicPlayer=null;GameState.onChanged=null;super.onDestroy()}
 private fun playNextTrack(){
  if(musicTracks.isEmpty()||isFinishing)return
  val uri=Uri.parse(musicTracks[musicIndex%musicTracks.size]);musicIndex++
  try{
   val player=MediaPlayer();musicPlayer=player
   player.setDataSource(this,uri);player.setVolume(.35f,.35f)
   player.setOnPreparedListener{if(!isFinishing)it.start()else it.release()}
   player.setOnCompletionListener{it.release();if(musicPlayer===it)musicPlayer=null;playNextTrack()}
   player.setOnErrorListener{mp,_,_->mp.release();if(musicPlayer===mp)musicPlayer=null;playNextTrack();true}
   player.prepareAsync()
  }catch(e:Exception){android.util.Log.w("AnimePushBattle","Falha ao tocar música",e);playNextTrack()}
 }
}

private class BattleView(context:android.content.Context):View(context){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG);private var tick=0;private var winResetScheduled=false
 private val d=resources.displayMetrics.density
 private val luffySprite=BitmapFactory.decodeResource(resources,R.drawable.luffy)
 private val narutoSprite=BitmapFactory.decodeResource(resources,R.drawable.naruto)
 private val giftBitmaps=ConcurrentHashMap<String,Bitmap>()
 private val giftLoading=ConcurrentHashMap.newKeySet<String>()
 private val giftImages=mapOf(
  "rose" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/eba3a9bb85c33e017f3648eaf88d7189~tplv-obj.webp",
  "tiny_diny" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/3851a489dea62b3a9fe99f61de5470ae~tplv-obj.webp",
  "paper_crane" to "https://p16-webcast.tiktokcdn.com/img/alisg/webcast-sg/0f158a08f7886189cdabf496e8a07c21~tplv-obj.webp",
  "galaxy" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/resource/79a02148079526539f7599150da9fd28.png~tplv-obj.webp",
  "white_rose" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/resource/a2d81f3847457be9083a9c76a59b08cb.png~tplv-obj.webp",
  "rosa" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/eb77ead5c3abb6da6034d3cf6cfeb438~tplv-obj.webp",
  "hat_mustache" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/2f1e4f3f5c728ffbfa35705b480fdc92~tplv-obj.webp",
  "dinosaur" to "https://p16-webcast.tiktokcdn.com/img/maliva/webcast-va/resource/e1b0687ef6df9d6db8222831d353fcf9.png~tplv-obj.webp"
 )
 private fun dp(v:Float)=v*d
 private fun argb(v:Long)=v.toInt()
 init{GameState.onChanged={postInvalidate()}}
 override fun onDraw(c:Canvas){
  try { drawFrame(c) } catch (e:RuntimeException) {
   p.shader=null;p.clearShadowLayer();p.style=Paint.Style.FILL;p.color=Color.rgb(12,14,28);c.drawColor(p.color)
   p.color=Color.WHITE;p.textAlign=Paint.Align.CENTER;p.textSize=dp(16f);p.typeface=Typeface.DEFAULT_BOLD
   c.drawText("ERRO NA ARENA",width/2f,height/2f-dp(12f),p)
   p.textSize=dp(11f);p.typeface=Typeface.DEFAULT
   val msg=e.message?.take(48) ?: e.javaClass.simpleName
   c.drawText(msg,width/2f,height/2f+dp(14f),p)
   android.util.Log.e("AnimePushBattle","Falha ao desenhar arena",e)
  }
 }
 private fun drawFrame(c:Canvas){
  val w=width.toFloat();val h=height.toFloat();if(w<=0||h<=0)return
  tick++;val s=GameState.snapshot()
  p.shader=LinearGradient(0f,0f,w,h,argb(0xff101c35),argb(0xff183c53),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,p);p.shader=null
  drawHeader(c,w,h,s)
  drawVillage(c,w,h)
  drawShip(c,w,h)
  val arenaTop=h*.355f;val arenaBottom=h*.655f
  drawArena(c,dp(12f),arenaTop,w-dp(12f),arenaBottom)
  drawVoteBanner(c,w,arenaTop)
  drawGiftDecorations(c,w*.285f,h*.505f,true)
  drawGiftDecorations(c,w*.715f,h*.505f,false)
  drawFighter(c,w*.285f,h*.505f,true)
  drawFighter(c,w*.715f,h*.505f,false)
  drawScoreBar(c,w*.285f,h*.505f-dp(94f),s.luffy,true)
  drawScoreBar(c,w*.715f,h*.505f-dp(94f),s.naruto,false)
  drawWall(c,w/2f+s.wall.coerceIn(-1f,1f)*w*.24f,h*.505f,h)
  s.impact?.let{
   p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(30f)
   p.setShadowLayer(dp(12f),0f,0f,Color.WHITE);p.color=Color.WHITE
   c.drawText("+${it.amount}",if(it.fighter==Fighter.LUFFY)w*.285f else w*.715f,arenaTop+dp(35f),p);p.clearShadowLayer()
  }
  s.winner?.let{
   p.color=0xdd080d19.toInt();c.drawRoundRect(w*.06f,h*.30f,w*.94f,h*.70f,dp(28f),dp(28f),p)
   p.style=Paint.Style.STROKE;p.strokeWidth=dp(4f);p.color=if(it==Fighter.LUFFY)argb(0xffffc56e) else argb(0xffff8c8c)
   c.drawRoundRect(w*.06f,h*.30f,w*.94f,h*.70f,dp(28f),dp(28f),p);p.style=Paint.Style.FILL
   p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(58f);p.color=Color.WHITE
   p.setShadowLayer(dp(22f),0f,0f,if(it==Fighter.LUFFY)argb(0xffffc56e) else argb(0xffff8c8c))
   c.drawText(if(it==Fighter.LUFFY)"LUFFY" else "NARUTO",w/2f,h*.475f,p)
   p.textSize=dp(48f);c.drawText("VITÓRIA!",w/2f,h*.57f,p);p.clearShadowLayer()
   if(!winResetScheduled){winResetScheduled=true;postDelayed({GameState.finishRound();winResetScheduled=false},5000L)}
  }
  drawParticles(c,w,h)
  postInvalidateDelayed(45)
 }
 private fun drawHeader(c:Canvas,w:Float,h:Float,s:BattleSnapshot){
  p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(27f);p.color=Color.WHITE
  p.setShadowLayer(dp(8f),0f,dp(2f),Color.BLACK);c.drawText("BATALHA ANIME",w/2,dp(43f),p);p.clearShadowLayer()
  p.textSize=dp(13f);p.color=argb(0xffffd083);c.drawText("LUFFY VS NARUTO",w/2,dp(64f),p)
 }
 private fun drawVoteBanner(c:Canvas,w:Float,top:Float){
  val messages=listOf(
   Pair("QUEM VAI VENCER ESSA BATALHA?","O CHAT DECIDE!"),
   Pair("LUFFY OU NARUTO?","ESCOLHA SEU LADO!"),
   Pair("NÃO DEIXE SEU FAVORITO PERDER!","COMENTE PARA DAR PONTOS!"),
   Pair("SERÁ QUE TEMOS UMA VIRADA?","A BATALHA AINDA NÃO ACABOU!"),
   Pair("QUEM MERECE SER O CAMPEÃO?","MOSTRE SUA TORCIDA!"),
   Pair("O DESTINO ESTÁ NAS MÃOS DO CHAT!","CADA COMENTÁRIO VALE +1 PONTO"),
   Pair("QUAL DOS DOIS É MAIS FORTE?","LUFFY VS NARUTO"),
   Pair("SEU PERSONAGEM PRECISA DE VOCÊ!","MANDE SEU APOIO NA LIVE!"),
   Pair("A ARENA VAI PEGAR FOGO!","QUEM LEVA ESSA?"),
   Pair("COMENTE LUFFY OU NARUTO!","AJUDE SEU FAVORITO A VENCER!")
  )
  val message=messages[(tick/120)%messages.size]
  val box=RectF(dp(20f),top+dp(9f),w-dp(20f),top+dp(53f))
  p.color=0xee101827.toInt();c.drawRoundRect(box,dp(12f),dp(12f),p)
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(1.5f);p.color=argb(0xffe8c47a);c.drawRoundRect(box,dp(12f),dp(12f),p);p.style=Paint.Style.FILL
  p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(13f);p.color=Color.WHITE
  c.drawText(message.first,w/2f,top+dp(27f),p)
  p.textSize=dp(11f);p.color=argb(0xffffd083)
  c.drawText(message.second,w/2f,top+dp(44f),p)
 }
 private fun drawScoreBar(c:Canvas,x:Float,y:Float,score:Int,left:Boolean){
  val color=if(left)argb(0xffffc56e) else argb(0xffff8c8c)
  val half=dp(53f);val top=y-dp(15f);val bottom=y+dp(15f)
  p.color=0xdd101827.toInt();c.drawRoundRect(x-half,top,x+half,bottom,dp(9f),dp(9f),p)
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(1.5f);p.color=color
  c.drawRoundRect(x-half,top,x+half,bottom,dp(9f),dp(9f),p);p.style=Paint.Style.FILL
  val progress=(score.coerceIn(0,1000)/1000f)
  p.color=0xff30394a.toInt();c.drawRoundRect(x-half+dp(5f),y+dp(2f),x+half-dp(5f),y+dp(7f),dp(3f),dp(3f),p)
  if(progress>0f){p.color=color;c.drawRoundRect(x-half+dp(5f),y+dp(2f),x-half+dp(5f)+(half*2-dp(10f))*progress,y+dp(7f),dp(3f),dp(3f),p)}
  p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.textSize=dp(14f);p.color=Color.WHITE
  c.drawText("$score PTS",x,y-dp(1f),p)
 }

 private fun drawVillage(c:Canvas,w:Float,h:Float){
  val top=dp(78f);val bottom=h*.345f
  p.shader=LinearGradient(0f,top,0f,bottom,argb(0xff101b35),argb(0xff9b5948),Shader.TileMode.CLAMP);c.drawRect(0f,top,w,bottom,p);p.shader=null
  // Lua velada e luzes distantes
  glow(c,w*.78f,top+dp(45f),dp(34f),argb(0x6688bce8))
  p.color=argb(0xffd9dce0);c.drawCircle(w*.78f,top+dp(45f),dp(16f),p)
  p.color=argb(0xffaeb9c6);c.drawCircle(w*.785f,top+dp(40f),dp(4f),p);c.drawCircle(w*.765f,top+dp(50f),dp(3f),p)
  // Montanhas sob uma tempestade distante
  p.color=argb(0xff27364a)
  val far=Path().apply{moveTo(0f,bottom-dp(35f));lineTo(w*.16f,bottom-dp(105f));lineTo(w*.31f,bottom-dp(57f));lineTo(w*.52f,bottom-dp(133f));lineTo(w*.72f,bottom-dp(58f));lineTo(w*.9f,bottom-dp(112f));lineTo(w,bottom-dp(55f));lineTo(w,bottom);lineTo(0f,bottom);close()};c.drawPath(far,p)
  p.color=argb(0xff182638)
  val near=Path().apply{moveTo(0f,bottom-dp(16f));lineTo(w*.18f,bottom-dp(62f));lineTo(w*.36f,bottom-dp(23f));lineTo(w*.61f,bottom-dp(72f));lineTo(w*.82f,bottom-dp(25f));lineTo(w,bottom-dp(54f));lineTo(w,bottom);lineTo(0f,bottom);close()};c.drawPath(near,p)
  // Silhuetas de torres destruídas
  drawRuinTower(c,w*.10f,bottom,dp(92f),dp(23f))
  drawRuinTower(c,w*.88f,bottom,dp(116f),dp(27f))
  drawRuinTower(c,w*.57f,bottom-dp(8f),dp(65f),dp(17f))
  // Braseiros e partículas de luz no horizonte
  for(i in 0..8){val x=w*(.08f+i*.105f);val y=bottom-dp((18+(i*13)%32).toFloat());p.color=argb(if(i%2==0)0xffffb56b else 0xff83c9ef);c.drawCircle(x,y,dp(if(i%3==0)2.2f else 1.2f),p)}
 }
 private fun drawRuinTower(c:Canvas,x:Float,base:Float,height:Float,halfWidth:Float){
  val l=x-halfWidth;val r=x+halfWidth;val t=base-height
  p.color=argb(0xff263345);c.drawRect(l+dp(5f),t+dp(12f),r-dp(5f),base,p)
  p.color=argb(0xff536174)
  val cap=Path().apply{moveTo(l-dp(5f),t+dp(13f));lineTo(l+dp(4f),t+dp(4f));lineTo(x-dp(4f),t+dp(8f));lineTo(x+dp(3f),t);lineTo(x+dp(8f),t+dp(9f));lineTo(r+dp(5f),t+dp(13f));close()};c.drawPath(cap,p)
  p.color=argb(0xff718094);p.strokeWidth=dp(2f)
  c.drawLine(l+dp(7f),t+dp(20f),l+dp(7f),base-dp(4f),p);c.drawLine(r-dp(7f),t+dp(20f),r-dp(7f),base-dp(4f),p)
  p.color=argb(0xff101a29);c.drawRect(x-dp(5f),base-dp(26f),x+dp(5f),base,p)
 }
  private fun drawHouse(c:Canvas,cx:Float,base:Float,houseW:Float,houseH:Float,wall:Int,roof:Int){
  val left=cx-houseW/2;val right=cx+houseW/2;val top=base-houseH
  p.color=argb(0x55332218);c.drawRoundRect(left-dp(4f),top+houseH*.28f,right+dp(4f),base+dp(3f),dp(4f),dp(4f),p)
  p.color=wall;c.drawRect(left,top+houseH*.3f,right,base,p)
  p.color=argb(0xffd7b887);c.drawRect(left+dp(4f),top+houseH*.34f,right-dp(4f),top+houseH*.39f,p)
  p.color=roof
  val r=Path().apply{moveTo(left-dp(10f),top+houseH*.34f);lineTo(cx,top-dp(5f));lineTo(right+dp(10f),top+houseH*.34f);lineTo(right-dp(2f),top+houseH*.39f);lineTo(cx,top+dp(7f));lineTo(left+dp(2f),top+houseH*.39f);close()};c.drawPath(r,p)
  p.color=argb(0xff593b32);c.drawRect(cx-dp(10f),base-dp(34f),cx+dp(10f),base,p)
  p.color=argb(0xff8ed6e5);c.drawRoundRect(left+dp(9f),top+houseH*.48f,left+dp(25f),top+houseH*.68f,dp(2f),dp(2f),p)
  c.drawRoundRect(right-dp(25f),top+houseH*.48f,right-dp(9f),top+houseH*.68f,dp(2f),dp(2f),p)
  p.color=argb(0xfff4dfad);p.strokeWidth=dp(1.5f)
  c.drawLine(left+dp(17f),top+houseH*.48f,left+dp(17f),top+houseH*.68f,p)
  c.drawLine(right-dp(17f),top+houseH*.48f,right-dp(17f),top+houseH*.68f,p)
 }
 private fun drawArena(c:Canvas,l:Float,t:Float,r:Float,b:Float){
  p.setShadowLayer(dp(18f),0f,dp(5f),argb(0xaa000000))
  p.shader=LinearGradient(l,t,r,b,argb(0xff272b43),argb(0xff101522),Shader.TileMode.CLAMP)
  c.drawRoundRect(l,t,r,b,dp(24f),dp(24f),p);p.shader=null;p.clearShadowLayer()
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(3f);p.color=argb(0xffe8c47a);c.drawRoundRect(l,t,r,b,dp(24f),dp(24f),p);p.style=Paint.Style.FILL
  // central fighting lane
  p.color=argb(0x22ffffff);c.drawRoundRect(l+dp(12f),t+dp(12f),r-dp(12f),b-dp(12f),dp(16f),dp(16f),p)
  p.color=argb(0x55ffffff);p.strokeWidth=dp(1f);c.drawLine((l+r)/2,t+dp(18f),(l+r)/2,b-dp(18f),p)
 }
 private fun glow(c:Canvas,x:Float,y:Float,r:Float,col:Int){
  p.shader=RadialGradient(x,y,r,col,0x00000000,Shader.TileMode.CLAMP);c.drawCircle(x,y,r,p);p.shader=null
 }
 private fun drawWall(c:Canvas,x:Float,y:Float,h:Float){
  val ww=dp(34f);val hh=h*.16f;val l=x-ww/2;val r=x+ww/2;val t=y-hh/2;val b=y+hh/2
  glow(c,x,y,dp(78f),argb(0x8899dfff))
  // stone core and metal frame
  p.setShadowLayer(dp(14f),0f,dp(3f),argb(0xcc000000))
  p.shader=LinearGradient(l,t,r,b,argb(0xffd9e0e8),argb(0xff596b80),Shader.TileMode.CLAMP)
  c.drawRoundRect(l,t,r,b,dp(8f),dp(8f),p);p.shader=null;p.clearShadowLayer()
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(3f);p.color=argb(0xfff0c878);c.drawRoundRect(l,t,r,b,dp(8f),dp(8f),p);p.style=Paint.Style.FILL
  // stone blocks
  p.color=argb(0xff8495a8);p.strokeWidth=dp(1.3f)
  for(i in 1..4){val yy=t+hh*i/5f;c.drawLine(l+dp(3f),yy,r-dp(3f),yy,p)}
  c.drawLine(x,t+dp(4f),x,b-dp(4f),p)
  // glowing central crystal
  glow(c,x,y,dp(27f),argb(0xff70d9ff))
  val crystal=Path().apply{moveTo(x,y-dp(22f));lineTo(x+dp(11f),y-dp(5f));lineTo(x+dp(7f),y+dp(17f));lineTo(x,y+dp(23f));lineTo(x-dp(8f),y+dp(8f));lineTo(x-dp(10f),y-dp(7f));close()}
  p.shader=LinearGradient(x-dp(10f),y-dp(20f),x+dp(10f),y+dp(20f),argb(0xffe5fbff),argb(0xff37a9e8),Shader.TileMode.CLAMP);c.drawPath(crystal,p);p.shader=null
  p.style=Paint.Style.STROKE;p.strokeWidth=dp(1.5f);p.color=Color.WHITE;c.drawPath(crystal,p);p.style=Paint.Style.FILL
 }
 private fun drawFighter(c:Canvas,x:Float,y:Float,left:Boolean){
  val bitmap=if(left)luffySprite else narutoSprite
  val targetHeight=dp(150f)
  val targetWidth=targetHeight*bitmap.width/bitmap.height
  val dst=RectF(x-targetWidth/2f,y-dp(76f),x+targetWidth/2f,y+dp(74f))
  p.alpha=255;p.isFilterBitmap=true;c.drawBitmap(bitmap,null,dst,p);p.isFilterBitmap=false
  p.textAlign=Paint.Align.CENTER;p.textSize=dp(17f);p.typeface=Typeface.create("sans-serif-black",Typeface.BOLD);p.color=Color.WHITE
  p.setShadowLayer(dp(5f),0f,dp(2f),Color.BLACK);c.drawText(if(left)"LUFFY" else "NARUTO",x,y+dp(99f),p);p.clearShadowLayer()
 }
 private fun drawGiftDecorations(c:Canvas,x:Float,y:Float,left:Boolean){
  val ids=if(left) listOf("rose","tiny_diny","paper_crane","galaxy") else listOf("white_rose","rosa","hat_mustache","dinosaur")
  val positions=listOf(
   Pair(-dp(54f),-dp(43f)),Pair(dp(53f),-dp(38f)),
   Pair(-dp(51f),dp(45f)),Pair(dp(54f),dp(43f))
  )
  ids.forEachIndexed{index,id->
   val (ox,oy)=positions[index];val size=dp(31f)
   val rect=RectF(x+ox-size/2f,y+oy-size/2f,x+ox+size/2f,y+oy+size/2f)
   p.color=0x66303b50;p.style=Paint.Style.FILL;c.drawCircle(rect.centerX(),rect.centerY(),size*.62f,p)
   val bitmap=giftBitmaps[id]
   if(bitmap!=null){p.alpha=210;p.isFilterBitmap=true;c.drawBitmap(bitmap,null,rect,p);p.alpha=255;p.isFilterBitmap=false}
   else loadGiftImage(id)
  }
 }
 private fun loadGiftImage(id:String){
  val imageUrl=giftImages[id]?:return
  if(!giftLoading.add(id))return
  Thread{
   try{val connection=URL(imageUrl).openConnection();connection.connectTimeout=8000;connection.readTimeout=8000;connection.setRequestProperty("User-Agent","Mozilla/5.0");val bitmap=connection.getInputStream().use{BitmapFactory.decodeStream(it)};if(bitmap!=null)giftBitmaps[id]=bitmap}
   catch(e:Exception){android.util.Log.w("AnimePushBattle","Não foi possível carregar ícone "+id,e)}
   finally{giftLoading.remove(id);postInvalidate()}
  }.start()
 }
 private fun drawShip(c:Canvas,w:Float,h:Float){
  val top=h*.68f;val bottom=h*.96f
  // Chão de pedra escura, com luz quente saindo das rachaduras
  p.shader=LinearGradient(0f,top,0f,bottom,argb(0xff4b5260),argb(0xff111a28),Shader.TileMode.CLAMP);c.drawRect(0f,top,w,bottom,p);p.shader=null
  val horizon=Path().apply{moveTo(0f,top+dp(32f));lineTo(w*.18f,top+dp(18f));lineTo(w*.36f,top+dp(36f));lineTo(w*.57f,top+dp(12f));lineTo(w*.78f,top+dp(31f));lineTo(w,top+dp(15f));lineTo(w,bottom);lineTo(0f,bottom);close()}
  p.color=argb(0xff303b4b);c.drawPath(horizon,p)
  // Colunas partidas em primeiro plano
  drawRuinTower(c,w*.055f,bottom,dp(152f),dp(25f))
  drawRuinTower(c,w*.94f,bottom,dp(174f),dp(29f))
  // Escombros espalhados pela arena
  for(i in 0..11){
   val x=w*(.04f+i*.087f);val y=top+dp((92+(i*31)%90).toFloat());val size=dp((8+(i%4)*4).toFloat())
   p.color=argb(if(i%2==0)0xff566273 else 0xff3d4a5b)
   val rock=Path().apply{moveTo(x-size,y+size*.4f);lineTo(x-size*.3f,y-size*.6f);lineTo(x+size*.8f,y-size*.2f);lineTo(x+size,y+size*.5f);close()};c.drawPath(rock,p)
  }
  // Fendas de energia sob o piso
  p.color=argb(0xffff9b55);p.strokeWidth=dp(2f)
  val cracks=listOf(Pair(w*.16f,top+dp(105f)),Pair(w*.79f,top+dp(156f)),Pair(w*.42f,top+dp(205f)))
  cracks.forEach{(x,y)->val path=Path().apply{moveTo(x-dp(20f),y);lineTo(x-dp(5f),y-dp(5f));lineTo(x+dp(3f),y+dp(7f));lineTo(x+dp(17f),y+dp(2f))};p.setShadowLayer(dp(8f),0f,0f,argb(0xffff8748));c.drawPath(path,p);p.clearShadowLayer()}
  // Névoa baixa para separar o cenário dos personagens
  p.shader=LinearGradient(0f,top+dp(18f),0f,bottom,0x003c9bc8,0x443c9bc8,Shader.TileMode.CLAMP);c.drawRect(0f,top+dp(18f),w,bottom,p);p.shader=null
 }
  private fun drawParticles(c:Canvas,w:Float,h:Float){
  for(i in 0..24){val x=((i*73+tick*2)%w.toInt().coerceAtLeast(1)).toFloat();val y=((i*137+tick*3)%(h.toInt().coerceAtLeast(1))).toFloat();p.color=if(i%2==0)0x55ffb36b else 0x554dbbff;c.drawCircle(x,y,dp(if(i%5==0)2.5f else 1f),p)}
 }
}
