require 'json'
package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name                = "RNScratch"
  s.version             = package["version"]
  s.description         = <<-DESC
                           RNScratch
                           DESC
  s.summary             = package['description']
  s.license             = package['license']
  s.homepage            = "https://github.com/ConduitMobileRND/react-native-scratch"
  s.authors             = "Invertase Limited"
  s.source              = { :git => "https://github.com/ConduitMobileRND/react-native-scratch.git", :tag => "v#{s.version}" }
  s.requires_arc        = true
  s.platform            = :ios, "9.0"
  s.source_files        = "ios/**/*.{h,m}"
  s.preserve_paths      = 'README.md', 'package.json', 'index.js'
  s.dependency          'React'


end
