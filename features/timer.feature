Feature: As a user I want to add and use the timers.

	Background: I start the app and slide to timer screen
		Given I start the app
		And I swipe right
		Then I see "TIMER"
		
	Scenario: I can add/delete a time to/from the timer
		When I press the button with id "timer_add"
		And I set the NumberPickers to 00 hours, 01 minutes, and 10 seconds
		And I press the "Save" button
		Then I will see "00:01:10" on the timer screen
		And I clear the timers
		And I will not see "00:01:10" on the timer screen


		