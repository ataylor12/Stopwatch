def stopwatch_is_running?
	begin
		performAction('assert_text', 'Pause', true)
		true
	rescue Exception => e
		false
	end
end

def setAmountOf(time, percent_from_left, percent_from_top)
	
  if time > 0
		for i in 1..time
			performAction('click_on_screen', percent_from_left, percent_from_top)
		end
	end
end
