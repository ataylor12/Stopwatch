def is_stopwatch_running?
	begin
		performAction('assert_text', 'Pause', true)
		true
	rescue Exception => e
		false
	end
end

def setAmountOf(time, at_this_location)
	if time > 0
		for i in 1..time
			performAction('click_on_screen', at_this_location, 25)
		end
	end
end