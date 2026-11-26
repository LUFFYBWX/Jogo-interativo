package com.livecraft

import android.content.Context

enum class Fighter { LUFFY, NARUTO }
data class GiftImpact(val fighter: Fighter, val amount: Int)
data class BattleSnapshot(val luffy: Int, val naruto: Int, val wall: Float, val winner: Fighter?, val impact: GiftImpact?)

object GameState {
 private const val PREFS="anime_push_battle"
 private val values=mapOf("Rose" to 1,"GG" to 5,"Ice Cream Cone" to 10,"Cake Slice" to 10,"Freestyle" to 20,"Glow Stick" to 20,"Pop" to 50,"Starfish" to 50,"TikTok" to 100,"Wink wink" to 100)
 private val sides=mapOf("Rose" to Fighter.LUFFY,"GG" to Fighter.NARUTO,"Ice Cream Cone" to Fighter.LUFFY,"Cake Slice" to Fighter.NARUTO,"Freestyle" to Fighter.LUFFY,"Glow Stick" to Fighter.NARUTO,"Pop" to Fighter.LUFFY,"Starfish" to Fighter.NARUTO,"TikTok" to Fighter.LUFFY,"Wink wink" to Fighter.NARUTO)
 private var prefs:android.content.SharedPreferences?=null
 private var luffy=0;private var naruto=0;private var impact:GiftImpact?=null;private var winner:Fighter?=null
 @Volatile var isRunning=false;private set
 @Volatile var onChanged:(()->Unit)?=null
 fun initialize(context:Context){if(prefs==null)prefs=context.applicationContext.getSharedPreferences(PREFS,Context.MODE_PRIVATE)}
 fun saveUsername(username:String){prefs?.edit()?.putString("username",username.trim().removePrefix("@"))?.apply()}
 fun loadUsername()=prefs?.getString("username","").orEmpty()
 @Synchronized fun startGame(){luffy=0;naruto=0;impact=null;winner=null;isRunning=true;onChanged?.invoke()}
 @Synchronized fun stopGame(){isRunning=false;onChanged?.invoke()}
 @Synchronized fun snapshot()=BattleSnapshot(luffy,naruto,(luffy-naruto).toFloat()/1000f,winner,impact)
 @Synchronized fun addGift(giftName:String,giftCount:Int,streakOver:Boolean):Int{
  if(!isRunning||!streakOver||winner!=null)return 0
  val key=values.keys.firstOrNull{normalize(it)==normalize(giftName)}?:return 0
  val amount=(values[key]?:1)*giftCount.coerceAtLeast(1);val side=sides[key]?:return 0
  if(side==Fighter.LUFFY){luffy+=amount;naruto=(naruto-amount).coerceAtLeast(0)}else{naruto+=amount;luffy=(luffy-amount).coerceAtLeast(0)}
  impact=GiftImpact(side,amount);if(luffy>=1000)winner=Fighter.LUFFY;if(naruto>=1000)winner=Fighter.NARUTO
  onChanged?.invoke();return if(winner==side)1 else 0
 }
 private fun normalize(s:String)=s.lowercase().filter{it.isLetterOrDigit()}
}