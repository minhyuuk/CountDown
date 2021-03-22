package com.example.countdown

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import cn.iwgang.countdownview.CountdownView
import com.example.countdown.databinding.ActivityMainBinding
import io.paperdb.Paper

// 1분 타이머 만들기

class MainActivity : AppCompatActivity() {

    private val LIMIT_TIME: Long = 60 * 1000 // 1분 타이머

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) } // 바인딩, 거의 매일 씀. 필수임.

    var isStart = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        Paper.init(this)

        isStart = Paper.book().read(IS_START_KEY, false)
        if (isStart) { // isStart는 초기값이 false이다. false일 때 시간을 확인합니다.
            binding.btnStart.isEnabled = false

            checkTime()

        } else {
            binding.btnStart.isEnabled = true
        }

        // 모든 이벤트들

        // 버튼을 누르면 시작됩니다
        binding.btnStart.setOnClickListener {
            if (!isStart) {
                binding.countdownView.start(LIMIT_TIME)
                Paper.book().write(IS_START_KEY, true)
            }
        }

        // 시간이 종료 되었을 때 띄웁니다
        binding.countdownView.setOnCountdownEndListener {
            Toast.makeText(this, "시간이 종료되었습니다", Toast.LENGTH_LONG).show()
            reset()
        }

        // 람다로 변환 가능, 남은 시간 띄우기
        binding.countdownView.setOnCountdownIntervalListener(1000,
            object : CountdownView.OnCountdownIntervalListener {
                override fun onInterval(cv: CountdownView?, remainTime: Long) {
                    Log.d("TIMER", "" + remainTime)
                }
            })
    }

    // 시간 멈추기
    override fun onStop() {
        Paper.book().write(TIME_REMAIN, binding.countdownView.remainTime)
        Paper.book().write(LAST_TIME_SAVED_KEY, System.currentTimeMillis())
        super.onStop()
    }

    // 시간 학인
    private fun checkTime() {
        val currentTime = System.currentTimeMillis()
        // 처음에 int로 설정하였으나 시간이 지나가지 않음.
        // Long으로 수정하니 잘 돌아감.
        val lastTimeSaved: Long = Paper.book().read<Long>(LAST_TIME_SAVED_KEY, 0)
        val timeRemain: Long = Paper.book().read(TIME_REMAIN, 0).toLong()
        val result = timeRemain + (lastTimeSaved - currentTime)
        if (result > 0) {
            binding.countdownView!!.start(result)
        } else {
            binding.countdownView.stop()
            reset()
        }
    }

    // 시간 리셋
    private fun reset() {
        binding.btnStart.isEnabled = true
        Paper.book().delete(IS_START_KEY)
        Paper.book().delete(LAST_TIME_SAVED_KEY)
        Paper.book().delete(TIME_REMAIN)

        isStart = false
    }

    // 텍스트
    companion object {
        private const val IS_START_KEY = "IS_START"
        private const val LAST_TIME_SAVED_KEY = "LAST TIME SAVED"
        private const val TIME_REMAIN = "TIME REMAIN"

    }
}