Feature: As a user I want to add and use the timers.

	Background: I start the app and slide to timer screen
		Given I start the app
		And I swipe right
		Then I see "TIMER"
		
	Scenario: I can add/delete a time to/from the timer
		When I press the button with id "timer_add"
		And I set the NumberPickers to 00 hours, 01 minutes, and 10 seconds
		And I press the "Save" button
		Then I see "00:01:10" on the timer screen
		And I clear the timers
		And I do not see "00:01:10" on the timer screen
		
	Scenario: I can reset add multiple timers
		When I press the button with id "timer_add"
		And I set the NumberPickers to 01 hours, 03 minutes, and 10 seconds
		And I press the "Save" button
		Then I will see "01:03:10" on the timer screen at position 1
		And I press the button with id "timer_add"
		And I set the NumberPickers to 00 hours, 00 minutes, and 01 seconds
		And I press the "Save" button
		Then I will see "01:03:10" on the timer screen at position 2
		And I clear the timers
		