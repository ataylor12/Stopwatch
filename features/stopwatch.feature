Feature: As a user I want to start and stop the stopwatch, track laps, and add and use the timers.

	Background: Ensure app has started
    	Given I start the app
		Then I should see "STOPWATCH"

	Scenario: I can start and stop the stopwatch
		When I press the button with id "toggle_stopwatch"
		And I wait
		And I press the "Pause" button
		Then the stopwatch should not be at "00:00"
		And I clear the stopwatch
		
	Scenario: I can reset the stopwatch
		When I press the button with id "toggle_stopwatch"
		And I wait
		And I press the "Pause" button
		And I press the button with id "clear_stopwatch"
		Then the stopwatch should be at "00:00"
		
	@thisone
	Scenario: I can record laps
		When I press the button with id "toggle_stopwatch"
		And I press the "Lap" button 2 times
		Then I can see two lap records on the screen
		And I clear the stopwatch
		
	@thisone
	Scenario: Lap 1's record is accurate
		When I press the button with id "toggle_stopwatch"
		And I press the "Lap" button 1 times
		Then the lap record will have matching times for lap time and total time
		And I clear the stopwatch
		
	Scenario: Lap 2's record is accurate
		When I press the button with id "toggle_stopwatch"
		And I press the "Lap" button 2 times
		Then the lap record will not have matching times for lap time and total time
		And I clear the stopwatch
		