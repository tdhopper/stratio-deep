#!/bin/bash
# Stratio Deep Deployment script

echo " >>> STRATIO DEEP DEPLOYMENT <<< "

if [ -z "$2" ]; then
echo "Usage: $0 -cv <cassandra version>"
  exit 0
fi

# Parse arguments
while (( "$#" )); do
case $1 in
    -cv)
      CASS_VER="$2"
      shift
      ;;
  esac
shift
done

LOCAL_DIR=`pwd`

echo "LOCAL_DIR=$LOCAL_DIR"

RELEASE_VER=""

#### Create Stratio Deep jars from bitbucket (master or specific tag?) through maven release plugin

# Clone Stratio Deep (master or specific tag?) from bitbucket
git clone git@bitbucket.org:stratio/stratiodeep.git ../stratiodeep-clone

cd ../stratiodeep-clone/StratioDeepParent/

# Execute maven release (creating release number and jars)
echo "Executing release:prepare"
mvn clean release:prepare -e -DignoreSnapshots=true -Darguments="-DskipTests"

exit 0

echo "Executing release:perform"
mvn release:perform -e -DignoreSnapshots=true -Darguments="-DskipTests"

cd ..

# (TODO) Get release number from maven

RELEASE_VER=$(grep "project.rel.com.pt.crawler[\:]*StratioDeepParent" release.properties | cut -d "=" -f2)

echo "RELEASE_VER=$RELEASE_VER"

echo "CASS_VER=$CASS_VER"

# Clone develop branch from bitbucket stratiospark project
git clone --branch develop git@bitbucket.org:stratio/stratiospark.git ../stratiospark-clone

cd ../stratiospark-clone

#### Using git-flow to create a new release branch

echo " >>> Creating git release $RELEASE_VER"

git flow release start $RELEASE_VER

echo " >>> Bumping version to $RELEASE_VER"

sed -i "s/DEEP_VER=[^$].*/DEEP_VER=$RELEASE_VER/g" stratio-deep-shell

sed -i "s/CASS_VER=[^$].*/CASS_VER=$CASS_VER/g" stratio-deep-shell

echo " >>> Commiting release $RELEASE_VER"

git commit -a -m "Bumped version to $RELEASE_VER"

echo " >>> Uploading new release branch to remote repository"

git flow release publish $RELEASE_VER

echo " >>> Executing make distribution script"

./make-distribution.sh --tgz --re $RELEASE_VER --cv $CASS_VER

#### Uploading the tgz file to a remote repository

echo " >>> Uploading the tgz distribution to a remote repository"

# (TODO) Upload to Stratio Nexus
#../Dropbox-Uploader/dropbox_uploader.sh -p upload stratio-spark-$RELEASE_VER-bin.tar.gz .

#rm stratio-spark-$RELEASE_VER-bin.tar.gz

echo " >>> Finishing"

#git checkout develop
git flow release finish $RELEASE_VER

cd ..

# Delete cloned stratiospark project
rm -rf stratiodeep-clone

# Delete cloned Stratio Deep project
rm -rf stratiospark-clone

cd "$LOCAL_DIR"

echo " >>> SCRIPT EXECUTION FINISHED <<< "