require 'calabash-android/calabash_steps'

Given /^I start the app$/ do
	#do nothing
end

And /^I press the button with id "([^\"]*)"$/ do |view_id|
	performAction('click_on_view_by_id', view_id)
end

Then /^the stopwatch should( not)? be at "([^\"]*)"$/ do |negate, value_stated|
	value_found = query("TextView id:'stopwatch_mm_ss'").first["text"]
	negate ? value_found.should_not == value_stated : value_found.should == value_stated
end

And /^I clear the stopwatch$/ do 
	if stopwatch_is_running?
		performAction('click_on_view_by_id', 'toggle_stopwatch')
	end
	performAction('click_on_view_by_id', 'clear_stopwatch')
end

When /^I press the "(.*?)" button (\d+ times)$/ do |button_name, num_times|
  for i in 1..num_times
  	performAction('press_button_with_text', button_name)
  end
end

Then /^I can see two lap records on the screen$/ do
	query("listview textview index:0").first["text"].should == "2"
	query("listview textview index:3").first["text"].should == "1"
end

Then /^the lap record will( not)? have matching times for lap time and total time$/ do |negate|
	lap_time = query("listview textview index:1").first["text"]
	total_time = query("listview textview index:2").first["text"]
	
	negate ? lap_time.should_not == total_time : lap_time.should == total_time
end

And /^I set the NumberPickers to (\d+ hours), (\d+ minutes), and (\d+ seconds)$/ do |hours, minutes, seconds|
	setAmountOf(hours, 25)
	setAmountOf(minutes, 50)
	setAmountOf(seconds, 75)
end

And /^I clear the timers$/ do
	performAction('press_long_on_text', 'Reset')
end
