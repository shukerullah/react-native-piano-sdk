require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-piano-sdk"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-piano-sdk
                   DESC
  s.homepage     = "https://github.com/shukerullah/react-native-piano-sdk"
  # brief license entry:
  s.license      = "MIT"
  # optional - use expanded license entry instead:
  # s.license    = { :type => "MIT", :file => "LICENSE" }
  s.authors      = { "Pir Shukarullah Shah" => "shuker_rashdi@yahoo.com" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/shukerullah/react-native-piano-sdk.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
  s.dependency "PianoComposer", "~> 2.3.13"
  s.dependency "PianoOAuth", "~> 2.3.13"
end

