Transform /^(\d+) times$/ do |num_of_times|
	num_of_times.to_i
end

Transform  /^(\d+) hours$/ do |hours|
	hours.to_i
end

Transform  /^(\d+) minutes$/ do |minutes|
	minutes.to_i
end

Transform  /^(\d+) seconds$/ do |seconds|
	seconds.to_i
end

