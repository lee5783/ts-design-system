SHELL := /bin/bash

VERSION := $(shell cat VERSION)

IOS_SCHEME := TSDesignSystem-Package
IOS_SNAPSHOT_DEVICE_SWIFT ?= ios/Tests/TSTestSupport/Snapshots/TSSnapshotDevice.swift
IOS_PINNED_DEVICE := $(shell sed -nE 's/^ +public static let pinnedSimulator = "([^"]+)"$$/\1/p' '$(IOS_SNAPSHOT_DEVICE_SWIFT)' 2>/dev/null)
IOS_PINNED_OS := $(shell sed -nE 's/^ +public static let pinnedOSVersion = "([^"]+)"$$/\1/p' '$(IOS_SNAPSHOT_DEVICE_SWIFT)' 2>/dev/null)
IOS_SIM_DEVICE ?= $(IOS_PINNED_DEVICE)
IOS_SIM_OS ?= $(IOS_PINNED_OS)
IOS_DERIVED_DATA ?= .build/xcode
IOS_RUNTIMES ?= pinned oldest
IOS_REQUIRE_OLDEST ?=
IOS_SIM_APPEARANCE ?=
SNAPSHOT_ARTIFACTS ?=
GRADLE_WORKERS ?=
XCODE_VERSION := $(shell cat .xcode-version)
CI_XCODE_APP ?= $(firstword $(wildcard $(foreach sep,_ -,/Applications/Xcode$(sep)$(XCODE_VERSION).app /Applications/Xcode$(sep)$(XCODE_VERSION).0.app)))
LAST_TAG_COMMAND := git describe --tags --abbrev=0 --match 'v[0-9]*.[0-9]*.[0-9]*' --match '[0-9]*.[0-9]*.[0-9]*' HEAD^ 2>/dev/null
RELEASE_NOTES ?= .build/release/notes.md
RELEASE_ASSETS ?= .build/release/assets
ANDROID_RELEASE_MODULES := tokens core compose views
IOS_SNAPSHOT_TEST_TARGETS := TSDesignSystemTests TSDesignSystemUIKitTests
DEMO_IOS_DERIVED_DATA ?= .build/xcode-demos
DEMO_IOS_SUPPORT_DIR := demos/ios-demo-support
DEMO_IOS_SWIFTUI_PROJECT := demos/ios-swiftui/TSDemoSwiftUI.xcodeproj
DEMO_IOS_SWIFTUI_SCHEME := TSDemoSwiftUI
DEMO_IOS_SWIFTUI_BUNDLE_ID := com.ts.ds.demo.swiftui
DEMO_IOS_SWIFTUI_AUDITS := TSDemoSwiftUIUITests/AccessibilityAuditTests
DEMO_IOS_SWIFTUI_THEME_TESTS := TSDemoSwiftUIUITests/ThemeOverrideTests
DEMO_IOS_UIKIT_PROJECT := demos/ios-uikit/TSDemoUIKit.xcodeproj
DEMO_IOS_UIKIT_SCHEME := TSDemoUIKit
DEMO_IOS_UIKIT_BUNDLE_ID := com.ts.ds.demo.uikit
DEMO_IOS_UIKIT_AUDITS := TSDemoUIKitUITests/AccessibilityAuditTests
DEMO_IOS_UIKIT_THEME_TESTS := TSDemoUIKitUITests/ThemeOverrideTests
DEMO_IOS_APP_ICON_SOURCE := design/assets/logos/normalized/ts-app-icon.svg
SWIFT_GOLDENS_DIR := tools/codegen/test/fixtures/expected/swift
XCRUN ?= xcrun
IOS_OLDEST_OS = $(shell $(XCRUN) simctl list runtimes available 2>/dev/null | sed -nE 's/^iOS ([0-9.]+) .*/\1/p' | sort -t. -k1,1n -k2,2n | head -1)
IOS_OLDEST_DEVICE ?= $(shell $(XCRUN) simctl list devices available 2>/dev/null | sed -nE '/^-- iOS $(IOS_OLDEST_OS) --$$/,/^-- /s/^ +(iPhone.*) [(][0-9A-F-]{36}[)] .*$$/\1/p' | head -1)

snapshot_artifacts_env = $(if $(SNAPSHOT_ARTIFACTS),TEST_RUNNER_SNAPSHOT_ARTIFACTS='$(abspath $(SNAPSHOT_ARTIFACTS))' )
gradle_flags = $(if $(GRADLE_WORKERS), -Dorg.gradle.workers.max=$(GRADLE_WORKERS))
ci_annotation = $(if $(GITHUB_ACTIONS),::$(1)::)

ios_test_on = printf 'ios-test: %s on iOS %s\n' '$(1)' '$(2)' && xcodebuild test -scheme $(IOS_SCHEME) -destination 'platform=iOS Simulator,name=$(1),OS=$(2)' -derivedDataPath $(IOS_DERIVED_DATA)
ios_destination = platform=iOS Simulator,name=$(1),OS=$(2)
demo_ios_support_test_on = printf 'demo-ios-support-test: %s on iOS %s\n' '$(1)' '$(2)' && cd $(DEMO_IOS_SUPPORT_DIR) && xcodebuild test -scheme TSDemoSupport -destination '$(call ios_destination,$(1),$(2))' -derivedDataPath $(CURDIR)/$(DEMO_IOS_DERIVED_DATA)
demo_ios_swiftui_test_on = printf 'demo-ios-swiftui-test: %s on iOS %s\n' '$(1)' '$(2)' && xcodebuild test -project $(DEMO_IOS_SWIFTUI_PROJECT) -scheme $(DEMO_IOS_SWIFTUI_SCHEME) -destination '$(call ios_destination,$(1),$(2))' -derivedDataPath $(DEMO_IOS_DERIVED_DATA) $(DEMO_IOS_TEST_SELECTION)
demo_ios_uikit_test_on = printf 'demo-ios-uikit-test: %s on iOS %s\n' '$(1)' '$(2)' && xcodebuild test -project $(DEMO_IOS_UIKIT_PROJECT) -scheme $(DEMO_IOS_UIKIT_SCHEME) -destination '$(call ios_destination,$(1),$(2))' -derivedDataPath $(DEMO_IOS_DERIVED_DATA) $(DEMO_IOS_TEST_SELECTION)
on_pinned_ios = $(if $(filter pinned,$(IOS_RUNTIMES)),$(call $(1),$(IOS_SIM_DEVICE),$(IOS_SIM_OS),pinned),true)
on_oldest_ios = $(if $(filter oldest,$(IOS_RUNTIMES)),$(if $(filter-out $(IOS_SIM_OS),$(IOS_OLDEST_OS)),$(if $(IOS_OLDEST_DEVICE),$(call $(1),$(IOS_OLDEST_DEVICE),$(IOS_OLDEST_OS)),$(ios_skip_oldest)),$(ios_no_older_runtime)),true)
ios_skip_oldest = printf '$(call ci_annotation,$(if $(IOS_REQUIRE_OLDEST),error,warning))ios-test: skipping iOS $(IOS_OLDEST_OS): no available iPhone simulator for that runtime; create one or set IOS_OLDEST_DEVICE\n' >&2$(if $(IOS_REQUIRE_OLDEST), && exit 1)
ios_pinned_udid = $(XCRUN) simctl list devices available | sed -nE '/^-- iOS $(IOS_SIM_OS) --$$/,/^-- /s/^    $(IOS_SIM_DEVICE) [(]([0-9A-F-]{36})[)].*$$/\1/p' | head -1
ios_sim_appearance = udid=$$($(ios_pinned_udid)) && test -n "$$udid" && { $(XCRUN) simctl boot "$$udid" 2>/dev/null || true; } && $(XCRUN) simctl bootstatus "$$udid" -b >/dev/null && $(XCRUN) simctl ui "$$udid" appearance '$(IOS_SIM_APPEARANCE)' && printf 'ci-ios-simulator: %s / iOS %s (%s) set to %s appearance\n' '$(IOS_SIM_DEVICE)' '$(IOS_SIM_OS)' "$$udid" '$(IOS_SIM_APPEARANCE)'
ios_no_older_runtime = $(if $(IOS_REQUIRE_OLDEST),printf '$(call ci_annotation,error)ios-test: no iOS runtime older than the pinned iOS $(IOS_SIM_OS) is available but IOS_REQUIRE_OLDEST=1 requires one: install an older iOS simulator runtime (xcodebuild -downloadPlatform iOS -buildVersion <version>) and an iPhone for it\n' >&2 && exit 1,$(if $(filter pinned,$(IOS_RUNTIMES)),true,printf '$(call ci_annotation,warning)ios-test: no iOS runtime older than the pinned iOS $(IOS_SIM_OS) is available: the oldest-runtime run has nothing to test\n' >&2))

.DEFAULT_GOAL := help

.PHONY: help ds-sync codegen codegen-check test tools-deps tools-test \
	ios-build ios-golden-typecheck ios-test ios-snapshot-verify ios-snapshot-record \
	android-build android-test android-snapshot-verify android-snapshot-record \
	android-publish-local android-publish \
	api-check android-api-check ios-api-check ios-api-check-record ios-api-check-last-tag \
	release-bump release-verify release-notes release-android-packages release-github \
	demo-ios-swiftui demo-ios-uikit demo-android-compose demo-android-views \
	demos-build demos-test demos-audit demos-check-boundaries \
	demo-ios-support-test demo-ios-swiftui-test demo-ios-swiftui-audit demo-ios-swiftui-theme-test demo-ios-swiftui-run demo-ios-app-icon \
	demo-ios-uikit-test demo-ios-uikit-audit demo-ios-uikit-theme-test demo-ios-uikit-run \
	demo-android-compose-test demo-android-views-test \
	actionlint ci-changes ci-required ci-xcode ci-ios-simulator ci-android-kvm

help:
	@printf 'TS Design System %s\n\nUsage: make <target>\n\n' '$(VERSION)'
	@printf '  %-26s %s\n' \
		'help' 'List the available targets' \
		'ds-sync' 'Verify design/source against design/manifest.json' \
		'codegen' 'Generate token and icon code for iOS and Android' \
		'codegen-check' 'Fail if generated code is out of date' \
		'test' 'Run tools-test, codegen-check, ds-sync, demos-check-boundaries, ios-test and android-test' \
		'tools-deps' 'Install the tools/ npm dependencies (npm ci), so later steps run without reinstalling' \
		'tools-test' 'Run the Vitest suites in tools/' \
		'ios-build' 'Build the Swift package for the iOS simulator' \
		'ios-golden-typecheck' 'Type-check the Swift golden fixtures of tools/codegen at iOS 15' \
		'ios-test' 'Run the iOS unit and behaviour tests; the snapshot suites skip (IOS_RUNTIMES=pinned|oldest)' \
		'ios-snapshot-verify' 'Compare the iOS snapshot suites with the local references on the pinned simulator (SNAPSHOT_ARTIFACTS=<dir> keeps failures)' \
		'ios-snapshot-record' 'Record the local iOS snapshot references (git-ignored, never committed)' \
		'android-build' 'Assemble the Android library modules' \
		'android-test' 'Run the Android unit and behaviour tests; the snapshot tests render without comparing' \
		'android-snapshot-verify' 'Run the Android tests and compare the snapshots with the local references' \
		'android-snapshot-record' 'Record the local Android snapshot references (git-ignored, never committed)' \
		'android-publish-local' 'Publish the Android artifacts and the BOM to ~/.m2 (mavenLocal)' \
		'android-publish' 'CI only: publish the Android modules not yet in GitHub Packages, the BOM last (GITHUB_ACTOR, GITHUB_TOKEN)' \
		'api-check' 'Check iOS and Android public API (BASELINE=<tag-or-ref> for iOS)' \
		'android-api-check' 'Check the Android public API against the in-tree api/*.api dumps' \
		'ios-api-check' 'Check the Swift library API against a git baseline (BASELINE=<tag-or-ref>)' \
		'ios-api-check-record' 'Acknowledge the current Swift API breaks against BASELINE=<ref> in ios/api/breaking-changes.txt' \
		'ios-api-check-last-tag' 'Run ios-api-check against the latest release tag before HEAD, or skip when there is none' \
		'release-bump' 'Bump VERSION and regenerate CHANGELOG.md with git-cliff (V=X.Y.Z, DRY_RUN=1)' \
		'release-verify' 'Fail unless TAG=vX.Y.Z equals VERSION, has a changelog section, and HEAD is on master (RELEASE_REF=refs/tags/vX.Y.Z)' \
		'release-notes' 'Write the CHANGELOG.md section of TAG=vX.Y.Z to RELEASE_NOTES' \
		'release-android-packages' 'Build the Android demo APKs and library AARs into RELEASE_ASSETS, named with VERSION' \
		'release-github' 'CI only: create the GitHub release for the pushed tag TAG=vX.Y.Z with the files in RELEASE_ASSETS' \
		'demo-ios-swiftui' 'Build the SwiftUI demo app (demos/ios-swiftui)' \
		'demo-ios-uikit' 'Build the UIKit demo app (demos/ios-uikit)' \
		'demo-android-compose' 'Build the Compose demo app (demos/android-compose)' \
		'demo-android-views' 'Build the Views demo app (demos/android-views)' \
		'demos-build' 'Build all four demo apps' \
		'demos-test' 'Run the demo tests, without the iOS accessibility audits' \
		'demos-audit' 'Run only the iOS demo accessibility audits (slow)' \
		'demos-check-boundaries' 'Fail if a demo app reaches library internals, or a design/specs/components spec has no page in every demo app' \
		'actionlint' 'Lint .github/workflows with a pinned, checksum-verified actionlint release in .build/tools' \
		'ci-changes' 'Print the CI areas touched since BASE=<commit> (and write GITHUB_OUTPUT)' \
		'ci-required' 'CI only: fail unless every job in NEEDS=<toJSON(needs)> passed, or was skipped by a successful changes job' \
		'ci-xcode' 'CI only: select the Xcode named in .xcode-version' \
		'ci-ios-simulator' 'CI only: software keyboard on, check the pinned simulator exists, and boot it in IOS_SIM_APPEARANCE=light|dark when set' \
		'ci-android-kvm' 'CI only: let the emulator use KVM on a Linux runner'

TOOLS_DEPS := tools/node_modules/.package-lock.json

$(TOOLS_DEPS): tools/package.json tools/package-lock.json
	npm --prefix tools ci --no-audit --no-fund

ds-sync: $(TOOLS_DEPS)
	npm --prefix tools run --silent ds-sync -- $${FROM:+--from "$$FROM"} $${DS_VERSION:+--version "$$DS_VERSION"} $(if $(UPDATE),--update)

codegen: $(TOOLS_DEPS)
	npm --prefix tools run --silent codegen -- $(or $(TARGET),all)

codegen-check: $(TOOLS_DEPS)
	npm --prefix tools run --silent codegen -- $(or $(TARGET),all) --check

test: tools-test codegen-check ds-sync demos-check-boundaries ios-test android-test

tools-test: $(TOOLS_DEPS)
	npm --prefix tools run --silent typecheck
	npm --prefix tools test

tools-deps: $(TOOLS_DEPS)

ios-build:
	xcodebuild build -scheme $(IOS_SCHEME) -destination 'generic/platform=iOS Simulator' -derivedDataPath $(IOS_DERIVED_DATA)

ios-golden-typecheck:
	$(XCRUN) --sdk iphonesimulator swiftc -typecheck -swift-version 6 -target $(shell uname -m)-apple-ios15.0-simulator $(SWIFT_GOLDENS_DIR)/*.swift

ios-test: ios-golden-typecheck
	@$(call on_pinned_ios,ios_test_on)
	@$(call on_oldest_ios,ios_test_on)

ios-snapshot-verify:
	@printf 'ios-snapshot-verify: comparing with the local references on %s / iOS %s\n' '$(IOS_SIM_DEVICE)' '$(IOS_SIM_OS)'
	TEST_RUNNER_TS_SNAPSHOT_VERIFY=1 TEST_RUNNER_TS_SNAPSHOT_REQUIRE_PINNED=1 $(snapshot_artifacts_env)xcodebuild test -scheme $(IOS_SCHEME) -destination 'platform=iOS Simulator,name=$(IOS_SIM_DEVICE),OS=$(IOS_SIM_OS)' -derivedDataPath $(IOS_DERIVED_DATA) $(IOS_SNAPSHOT_TEST_TARGETS:%=-only-testing:%)

ios-snapshot-record:
	@printf 'ios-snapshot-record: recording on %s / iOS %s\n' '$(IOS_SIM_DEVICE)' '$(IOS_SIM_OS)'
	TEST_RUNNER_TS_SNAPSHOT_RECORD=1 xcodebuild test -scheme $(IOS_SCHEME) -destination 'platform=iOS Simulator,name=$(IOS_SIM_DEVICE),OS=$(IOS_SIM_OS)' -derivedDataPath $(IOS_DERIVED_DATA) $(IOS_SNAPSHOT_TEST_TARGETS:%=-only-testing:%)

android-build:
	cd android && ./gradlew assemble lint apiCheck$(gradle_flags)

android-test:
	cd android && ./gradlew test$(gradle_flags)

android-snapshot-verify:
	cd android && ./gradlew test -Proborazzi.test.verify=true$(gradle_flags)

android-snapshot-record:
	cd android && ./gradlew recordRoborazziDebug$(gradle_flags)

android-publish-local:
	cd android && ./gradlew publishToMavenLocal$(gradle_flags)

android-publish: $(TOOLS_DEPS)
	@test -n "$$GITHUB_REPOSITORY" -a -n "$$GITHUB_ACTOR" -a -n "$$GITHUB_TOKEN" || { printf 'android-publish: GITHUB_REPOSITORY, GITHUB_ACTOR and GITHUB_TOKEN must be set; it runs from release.yml\n' >&2; exit 1; }
	npm --prefix tools run --silent publish-android --$(if $(GRADLE_WORKERS), --workers '$(GRADLE_WORKERS)')

api-check: ios-api-check android-api-check

android-api-check:
	cd android && ./gradlew apiCheck$(gradle_flags)

ios-api-check: $(TOOLS_DEPS)
	npm --prefix tools run --silent api-check-ios -- $${BASELINE:+--baseline "$$BASELINE"}

ios-api-check-record: $(TOOLS_DEPS)
	npm --prefix tools run --silent api-check-ios -- $${BASELINE:+--baseline "$$BASELINE"} --record

ios-api-check-last-tag:
	@tag="$$($(LAST_TAG_COMMAND))"; if [ -n "$$tag" ]; then BASELINE="$$tag" $(MAKE) --no-print-directory ios-api-check; else printf 'ios-api-check-last-tag: no tag yet, so there is no release API to compare against\n'; fi

release-bump: $(TOOLS_DEPS)
	npm --prefix tools run --silent release -- bump --version "$$V"$(if $(DRY_RUN), --dry-run)

release-verify: $(TOOLS_DEPS)
	npm --prefix tools run --silent release -- verify --tag "$$TAG" $${RELEASE_REF:+--ref "$$RELEASE_REF"}

release-notes: $(TOOLS_DEPS)
	npm --prefix tools run --silent release -- notes --tag "$$TAG" --out '$(RELEASE_NOTES)'

release-android-packages:
	cd demos/android-compose && ./gradlew :app:assembleDebug$(gradle_flags)
	cd demos/android-views && ./gradlew :app:assembleDebug$(gradle_flags)
	cd android && ./gradlew $(ANDROID_RELEASE_MODULES:%=:%:assembleRelease)$(gradle_flags)
	mkdir -p '$(RELEASE_ASSETS)'
	cp demos/android-compose/app/build/outputs/apk/debug/app-debug.apk '$(RELEASE_ASSETS)/ts-ds-demo-compose-$(VERSION).apk'
	cp demos/android-views/app/build/outputs/apk/debug/app-debug.apk '$(RELEASE_ASSETS)/ts-ds-demo-views-$(VERSION).apk'
	$(foreach module,$(ANDROID_RELEASE_MODULES),cp android/$(module)/build/outputs/aar/$(module)-release.aar '$(RELEASE_ASSETS)/ts-ds-$(module)-$(VERSION).aar' &&) true

release-github: release-notes
	gh release create "$$TAG" --verify-tag --title "$$TAG" --notes-file '$(RELEASE_NOTES)' $(wildcard $(RELEASE_ASSETS)/*)

demo-ios-swiftui:
	xcodebuild build -project $(DEMO_IOS_SWIFTUI_PROJECT) -scheme $(DEMO_IOS_SWIFTUI_SCHEME) -destination 'generic/platform=iOS Simulator' -derivedDataPath $(DEMO_IOS_DERIVED_DATA)

demo-ios-swiftui-run:
	xcodebuild build -project $(DEMO_IOS_SWIFTUI_PROJECT) -scheme $(DEMO_IOS_SWIFTUI_SCHEME) -destination '$(call ios_destination,$(IOS_SIM_DEVICE),$(IOS_SIM_OS))' -derivedDataPath $(DEMO_IOS_DERIVED_DATA)
	$(XCRUN) simctl boot '$(IOS_SIM_DEVICE)' 2>/dev/null || true
	$(XCRUN) simctl install booted $(DEMO_IOS_DERIVED_DATA)/Build/Products/Debug-iphonesimulator/$(DEMO_IOS_SWIFTUI_SCHEME).app
	$(XCRUN) simctl launch booted $(DEMO_IOS_SWIFTUI_BUNDLE_ID) $(DEMO_ARGS)

demo-ios-support-test:
	@$(call on_pinned_ios,demo_ios_support_test_on)
	@$(call on_oldest_ios,demo_ios_support_test_on)

demo-ios-swiftui-test: DEMO_IOS_TEST_SELECTION = -skip-testing:$(DEMO_IOS_SWIFTUI_AUDITS)
demo-ios-swiftui-test:
	@$(call on_pinned_ios,demo_ios_swiftui_test_on)
	@$(call on_oldest_ios,demo_ios_swiftui_test_on)

demo-ios-swiftui-audit: DEMO_IOS_TEST_SELECTION = -only-testing:$(DEMO_IOS_SWIFTUI_AUDITS)
demo-ios-swiftui-audit:
	@$(call demo_ios_swiftui_test_on,$(IOS_SIM_DEVICE),$(IOS_SIM_OS))

demo-ios-swiftui-theme-test: DEMO_IOS_TEST_SELECTION = -only-testing:$(DEMO_IOS_SWIFTUI_THEME_TESTS)
demo-ios-swiftui-theme-test:
	@$(call demo_ios_swiftui_test_on,$(IOS_SIM_DEVICE),$(IOS_SIM_OS))

demo-ios-app-icon:
	$(XCRUN) swift tools/demos/src/render-app-icon.swift $(DEMO_IOS_APP_ICON_SOURCE) demos/ios-swiftui/TSDemoSwiftUI/App/Assets.xcassets/AppIcon.appiconset/app-icon.png 1024
	cp demos/ios-swiftui/TSDemoSwiftUI/App/Assets.xcassets/AppIcon.appiconset/app-icon.png demos/ios-uikit/TSDemoUIKit/App/Assets.xcassets/AppIcon.appiconset/app-icon.png

demo-ios-uikit:
	xcodebuild build -project $(DEMO_IOS_UIKIT_PROJECT) -scheme $(DEMO_IOS_UIKIT_SCHEME) -destination 'generic/platform=iOS Simulator' -derivedDataPath $(DEMO_IOS_DERIVED_DATA)

demo-ios-uikit-run:
	xcodebuild build -project $(DEMO_IOS_UIKIT_PROJECT) -scheme $(DEMO_IOS_UIKIT_SCHEME) -destination '$(call ios_destination,$(IOS_SIM_DEVICE),$(IOS_SIM_OS))' -derivedDataPath $(DEMO_IOS_DERIVED_DATA)
	$(XCRUN) simctl boot '$(IOS_SIM_DEVICE)' 2>/dev/null || true
	$(XCRUN) simctl install booted $(DEMO_IOS_DERIVED_DATA)/Build/Products/Debug-iphonesimulator/$(DEMO_IOS_UIKIT_SCHEME).app
	$(XCRUN) simctl launch booted $(DEMO_IOS_UIKIT_BUNDLE_ID) $(DEMO_ARGS)

demo-ios-uikit-test: DEMO_IOS_TEST_SELECTION = -skip-testing:$(DEMO_IOS_UIKIT_AUDITS)
demo-ios-uikit-test:
	@$(call on_pinned_ios,demo_ios_uikit_test_on)
	@$(call on_oldest_ios,demo_ios_uikit_test_on)

demo-ios-uikit-audit: DEMO_IOS_TEST_SELECTION = -only-testing:$(DEMO_IOS_UIKIT_AUDITS)
demo-ios-uikit-audit:
	@$(call demo_ios_uikit_test_on,$(IOS_SIM_DEVICE),$(IOS_SIM_OS))

demo-ios-uikit-theme-test: DEMO_IOS_TEST_SELECTION = -only-testing:$(DEMO_IOS_UIKIT_THEME_TESTS)
demo-ios-uikit-theme-test:
	@$(call demo_ios_uikit_test_on,$(IOS_SIM_DEVICE),$(IOS_SIM_OS))

demo-android-compose:
	cd demos/android-compose && ./gradlew :app:assembleDebug :app:lintDebug :android-demo-support:testDebugUnitTest :android-demo-support:lintDebug$(gradle_flags)

demo-android-compose-test:
	cd demos/android-compose && ./gradlew :android-demo-support:testDebugUnitTest :app:connectedDebugAndroidTest$(gradle_flags)

demo-android-views:
	cd demos/android-views && ./gradlew :app:assembleDebug :app:lintDebug :android-demo-support:testDebugUnitTest :android-demo-support:lintDebug$(gradle_flags)

demo-android-views-test:
	cd demos/android-views && ./gradlew :android-demo-support:testDebugUnitTest :app:connectedDebugAndroidTest$(gradle_flags)

demos-build: demo-ios-swiftui demo-ios-uikit demo-android-compose demo-android-views

demos-test: demo-ios-support-test demo-ios-swiftui-test demo-ios-uikit-test demo-android-compose-test demo-android-views-test

demos-audit: demo-ios-swiftui-audit demo-ios-uikit-audit

demos-check-boundaries: $(TOOLS_DEPS)
	npm --prefix tools run --silent demos-check-boundaries
	npm --prefix tools run --silent demos-check-catalog

actionlint: $(TOOLS_DEPS)
	npm --prefix tools run --silent actionlint

ci-changes: $(TOOLS_DEPS)
	npm --prefix tools run --silent ci-changes -- $${BASE:+--base "$$BASE"}

ci-required: $(TOOLS_DEPS)
	npm --prefix tools run --silent ci-required

ci-xcode:
	@test -n '$(CI_XCODE_APP)' || { printf 'ci-xcode: Xcode %s is not in /Applications; found: %s\n' '$(XCODE_VERSION)' '$(wildcard /Applications/Xcode*.app)' >&2; exit 1; }
	@printf 'DEVELOPER_DIR=%s/Contents/Developer\n' '$(CI_XCODE_APP)' >> "$${GITHUB_ENV:?ci-xcode writes DEVELOPER_DIR to GITHUB_ENV, so it only runs in GitHub Actions}"
	DEVELOPER_DIR='$(CI_XCODE_APP)/Contents/Developer' xcodebuild -version

ci-ios-simulator:
	defaults write com.apple.iphonesimulator ConnectHardwareKeyboard -bool false
	@if [ -n '$(filter pinned,$(IOS_RUNTIMES))' ] && ! $(XCRUN) simctl list devices available | sed -nE '/^-- iOS $(IOS_SIM_OS) --$$/,/^-- /p' | grep -qF '    $(IOS_SIM_DEVICE) ('; then printf 'ci-ios-simulator: no available %s simulator on iOS %s; the snapshots are pinned to it. Runtimes:\n' '$(IOS_SIM_DEVICE)' '$(IOS_SIM_OS)' >&2; $(XCRUN) simctl list runtimes >&2; exit 1; fi
	@$(if $(IOS_SIM_APPEARANCE),$(if $(filter-out light dark,$(IOS_SIM_APPEARANCE)),printf 'ci-ios-simulator: IOS_SIM_APPEARANCE must be light or dark; got %s\n' '$(IOS_SIM_APPEARANCE)' >&2; exit 1,$(if $(filter pinned,$(IOS_RUNTIMES)),$(ios_sim_appearance),true)),true)

ci-android-kvm:
	echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
	sudo udevadm control --reload-rules
	sudo udevadm trigger --name-match=kvm
