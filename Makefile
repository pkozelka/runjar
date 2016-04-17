install:
	mvn clean install

site:
	mvn site

	# fix refs to JXR line numbers
	find target/site runjar-*/target/site -name '*.html' -print0 | xargs -0 sed -i 's:\(\.html#\)L\([[:digit:]]\):\1\2:g'

	git fetch origin +gh-pages:gh-pages
	rm -rf target/site-repo
	git clone . target/site-repo -b gh-pages --single-branch
	cp -a target/site/. target/site-repo/
	cp -a runjar-boot/target/site/. target/site-repo/runjar-boot/
	cp -a runjar-maven-plugin/target/site/. target/site-repo/runjar-maven-plugin/

	# fixes in html
	find target/site-repo -name '*.html' | xargs sed -i 's:<a href="./":<a href="index.html":g'
	find target/site-repo/*/* -name '*.html' | xargs sed -i 's:<a href="index.html" title="runjar":<a href="../index.html" title="runjar":g'
	#

	cd target/site-repo && git add -A && git commit -m 'updated site' && git push

site-deploy:
	git push origin gh-pages

gitsite:
	mvn clean install site site:stage site-deploy -e
